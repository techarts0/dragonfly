/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.xkit.web;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.techarts.whale.Context;
import cn.techarts.xkit.app.helper.Converter;
import cn.techarts.xkit.app.helper.Empty;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.Scanner;
import cn.techarts.xkit.web.restful.Restful;

/**
 * @author rocwon@gmail.com
 */
public class StartupListener implements ServletContextListener {
	private boolean standalone = false; //Running without DI
	//The constant MUST be same as Context.NAME in whale project.
	public static final String WHALE_KEY = "context.whale.techarts";
	public static final String CONFIG_PATH = "contextConfigLocation";
	private static final Logger LOGGER = Hotpot.getLogger();
		
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var classpath = this.getRootClassPath();
		var classes = this.scanClasses(classpath);
		if(Empty.is(classes)) return;
		var config = getResourcePath("config.properties");
		var configs = Hotpot.resolveProperties(config);
		this.getSessionConfig(context, configs);
		this.standalone = isRunningStandalone(context);
		if(!standalone) initWhale(context, classes, configs);
		int n = this.initWebServices(context, classes);
		LOGGER.info("The web application has been started successfully. (" + n + " web services)");
	}
	
	private List<String> scanClasses(String classpath){
		var base = new File(classpath);//Root class-path
		if(!base.isDirectory()) return null;
		var start = base.getAbsolutePath().length();
		return Scanner.scanClasses(base, start);
	}
	
	private void getSessionConfig(ServletContext context, Map<String, String> configs) {
		var result = new SessionConfig();
		try {
			result.setSessionKey(configs.remove("session.key"));
			result.setSessionSalt(configs.remove("session.salt"));
			var duration = configs.remove("session.duration");		
			result.setSessionDuration(Converter.toInt(duration));
			var permission = configs.remove("session.check");
			result.setSessionCheck(Converter.toBoolean(permission));
			var uid = configs.remove("session.uidProperty");
			result.setUidProperty(uid == null ? "uid" : uid);
		}catch(Exception e) {
			//Ignored. Returns an empty SessionConfig object.
		}
		context.setAttribute(SessionConfig.CACHE_KEY, result);
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(Objects.isNull(result) || result.getPath() == null){
			throw new RuntimeException("Failed to get class path.");
		}
		return result.getPath();
	}
	
	private String getResourcePath(String resource) {
		var result = getClass().getResource("/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		result = getClass().getResource("/WEB-INF/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		throw new RuntimeException("Failed to find the resource: [" + resource + "]");
	}
	
	private String getBeansXmlWithoutException() {
		try {
			return getResourcePath("beans.xml");
		}catch(RuntimeException e) {
			return null;
		}
	}
	
	private void initWhale(ServletContext context, List<String> classes, Map<String, String> configs) {
		var ctx = Context.make(configs).cache(context);
		ctx.getBinder().register(classes);
		ctx.getLoader().parse(getBeansXmlWithoutException());
		registerApplicationModules(ctx, configs.get("app.modules"));
		ctx.start();
	}
	
	private void registerApplicationModules(Context context, String modules) {
		if(Empty.is(modules)) return;
		var appModules = modules.split(",");
		for(int i = 0; i < appModules.length; i++) {
			var module = appModules[i].trim();
			if(module.endsWith(".jar")) {
				context.getLoader().load(module);
			}else { // .class
				context.getBinder().register(module);
			}
		}
	}
	
	private boolean isRunningStandalone(ServletContext arg) {
		try {
			Class.forName("cn.techarts.whale.Panic");
			return false;
		}catch(ClassNotFoundException e) {
			return true;
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if(this.standalone) return;
		var ctx = arg0.getServletContext();
		var context = Context.from(ctx);
		if(context != null) context.close();
	}
	
	private String getWebService(Object obj) {
		if(Objects.isNull(obj)) return null;
		var clazz = obj.getClass();
		var resource = clazz.getAnnotation(Restful.class);
		if(resource != null) return resource.value();
		var jsr371 = clazz.getAnnotation(Controller.class);
		return Objects.isNull(jsr371) ? null : jsr371.value();
	}
	
	private int initWebServices(ServletContext context, List<String> classes) {
		var webServiceCount = 0;
		var root = new WebLocator(false);
		for(var service : classes) {
			var ws = getWS(context, service);
			if(Objects.isNull(ws)) continue;
			var prefix = getWebService(ws);
			if(Objects.isNull(prefix)) continue;
			var methods = ws.getClass().getMethods();
			if(methods.length == 0) continue;
			for(var method : methods) {
				if(!checkParamType(method)) continue;
				var meta = ServiceMeta.to(method, ws, prefix);
				webServiceCount += root.parse(meta);
			}
			context.setAttribute(WebLocator.CACHE_KEY, root);
		}
		return webServiceCount; //How many web-services are found?
	}
	
	private Object getWS(ServletContext context, String clazz) {
		if(!standalone) {
			var tmp = Context.from(context);
			return tmp.silent(clazz);
		}else {
			try {
				return Class.forName(clazz);
			}catch(ClassNotFoundException e) {
				return null;
			}
		}
	}
	
	private boolean checkParamType(Method m) {
		var pts = m.getParameterTypes();
		if(pts.length != 1) return false;
		var ptn = WebContext.class.getName();
		return ptn.equals(pts[0].getName());
	}	
}