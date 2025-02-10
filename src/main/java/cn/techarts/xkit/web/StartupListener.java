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

//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletContextEvent;
//import jakarta.servlet.ServletContextListener;

import cn.techarts.whale.Context;
import cn.techarts.xkit.app.helper.Converter;
import cn.techarts.xkit.app.helper.Empty;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.Scanner;
import cn.techarts.xkit.web.rest.Restful;
import cn.techarts.xkit.web.token.TokenConfig;

/**
 * <p>javax & jakarta</p>
 * @author rocwon@gmail.com
 */
public class StartupListener implements ServletContextListener {
	private boolean standalone = false; //Running without DI
	public static final String DB_CONFIG = "jdbc.properties";
	public static final String URL_PATTERN = "web.url.pattern";
	public static final String APP_CONFIG = "application.properties";
	private static final Logger LOGGER = Hotpot.getLogger();
		
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var classpath = this.getRootClassPath();
		var classes = this.scanClasses(classpath);
		if(classes == null || classes.isEmpty()) return;
		var config = getResourcePath(APP_CONFIG, false);
		var configs = Hotpot.resolveProperties(config);
		this.appendDatabaseProperties(configs);
		this.getTokenConfig(context, configs);
		this.standalone = isRunningStandalone();
		if(!standalone) initWhale(context, classes, configs);
		int n = this.initWebServices(context, classes);
		registerServiceRouter(context, configs.get(URL_PATTERN));
		LOGGER.info("The web application has been started successfully. (" + n + " web services)");
	}
	
	private void appendDatabaseProperties(Map<String, String> configs) {
		var tmp = getResourcePath(DB_CONFIG, true);
		var database = Hotpot.resolveProperties(tmp);
		if(database != null && !database.isEmpty()) {
			configs.putAll(database);
		}
	}
	
	private void registerServiceRouter(ServletContext context, String urlPattern) {
		var pattern = urlPattern == null ? "/ws/*" : urlPattern;
		context.addServlet("serviceRouter",ServiceRouter.class).addMapping(pattern);
	}
	
	private List<String> scanClasses(String classpath){
		var base = new File(classpath);//Root class-path
		if(!base.isDirectory()) return null;
		var start = base.getAbsolutePath().length();
		return Scanner.scanClasses(base, start);
	}
	
	private void getTokenConfig(ServletContext context, Map<String, String> configs) {
		var result = new TokenConfig();
		try {
			result.setKey(configs.remove("token.key"));
			result.setSalt(configs.remove("token.salt"));
			var duration = configs.remove("token.duration");
			result.setDuration(Converter.toInt(duration));
			var mandatory = configs.remove("token.mandatory");
			result.setMandatory(Converter.toBoolean(mandatory));
			var uid = configs.remove("token.uidProperty");
			result.setUidProperty(uid == null ? "uid" : uid);
			result.setTokenizer(configs.remove("token.tokenizer"));
		}catch(Exception e) {
			//Ignored. Returns an empty SessionConfig object.
		}
		context.setAttribute(TokenConfig.CACHE_KEY, result);
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(Objects.isNull(result) || result.getPath() == null){
			throw new RuntimeException("Failed to find class path.");
		}
		return result.getPath();
	}
	
	private String getResourcePath(String resource, boolean silence) {
		var result = getClass().getResource("/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		result = getClass().getResource("/WEB-INF/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		if(silence) {
			return null; //Don't throw an exception
		}else {
			throw new RuntimeException("Failed to find the resource: [" + resource + "]");
		}
	}
	
	private void initWhale(ServletContext context, List<String> classes, Map<String, String> configs) {
		var ctx = Context.make(configs).cache(context);
		ctx.getBinder().register(classes);
		ctx.getLoader().parse(getResourcePath("beans.xml", false));
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
	
	private boolean isRunningStandalone() {
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