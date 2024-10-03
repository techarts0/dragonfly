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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.techarts.whale.Context;
import cn.techarts.xkit.data.DataManager;
import cn.techarts.xkit.data.redis.RedisCacheHelper;
import cn.techarts.xkit.util.Converter;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.Scanner;

/**
 * @author rocwon@gmail.com
 */
public class StartupListener implements ServletContextListener {
	public static final String CONFIG_PATH = "contextConfigLocation";
	private static final Logger LOGGER = Hotpot.getLogger();
		
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var classpath = this.getRootClassPath();
		var classes = this.scanClasses(classpath);
		if(classes == null || classes.isEmpty()) return;
		var config = getResourcePath("config.properties");
		var configs = Hotpot.resolveProperties(config);
		
		this.getSessionConfig(context, configs);
		this.initWhale(context, classes, configs);
		int n = initWebServices(context, classes);
		LOGGER.info("The web application is started: (" + n + " web services)");
	}
	
	private List<String> scanClasses(String classpath){
		var base = new File(classpath);//Root class-path
		if(base == null || !base.isDirectory()) return null;
		var start = base.getAbsolutePath().length();
		return Scanner.scanClasses(base, start);
	}
	
	private void getSessionConfig(ServletContext context, Map<String, String> configs) {
		var result = new SessionConfig();
		result.setSessionKey(configs.remove("session.key"));
		result.setSessionSalt(configs.remove("session.salt"));
		var duration = configs.remove("session.duration");		
		result.setSessionDuration(Converter.toInt(duration));
		var permission = configs.remove("session.check");
		result.setSessionCheck(Converter.toBoolean(permission));
		context.setAttribute(SessionConfig.CACHE_KEY, result);
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(result == null || result.getPath() == null){
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
	
	private void initWhale(ServletContext context, List<String> classes, Map<String, String> configs) {
		Context.make(configs)
		       .cache(context)
		       .createFactory()
		       .register(classes) //scan(classpath)
		       .parse(getResourcePath("beans.xml"))
		       .register(DataManager.class)
		       .register(RedisCacheHelper.class)
		       .start();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		var ctx = arg0.getServletContext();
		var context = Context.from(ctx);
		if(context != null) context.close();
	}
	
	public<T> T get(ServletContext ctx, String id, Class<T> clzz)
	{
		return Context.from(ctx).get(id, clzz);
	}
	
	public Object get(ServletContext ctx, String id){
		return Context.from(ctx).get(id);
	}
	
	private boolean isWebService(Object obj) {
		if(obj == null) return false;
		var clazz = obj.getClass();
		return clazz.isAnnotationPresent(WebService.class);
	}
	
	private int initWebServices(ServletContext context, List<String> classes) {
		var container = Context.from(context);
		
		var result = new LinkedHashMap<String, ServiceMeta>(512);
		
		for(var service : classes) {
			var ws = container.silent(service);
			if(ws == null || !isWebService(ws)) continue;
			var methods = ws.getClass().getMethods();
			if(methods == null || methods.length == 0) continue;
			for(var method : methods) {
				var wm = method.getAnnotation(WebMethod.class);
				//The method is not a (or not a legal) web service
				if(wm == null || wm.uri() == null) continue;
				if(!checkMethodParameterType(method)) continue;
				var s = new ServiceMeta(wm.uri(), ws, method, wm.method());
				s.setPermissionRequired(wm.permission()); //Session
				if(!ServiceMeta.restful) {
					result.put(wm.uri(), s); //Without the prefix get|post 
				}else { //With a prefix string get|post such as get/user/login
					var tmp = wm.method().toLowerCase();
					result.put(tmp.concat(wm.uri()), s);
				}
			}
			if(!result.isEmpty()) context.setAttribute(WebService.CACHE_KEY, result);
		}
		return result.size(); //How many web-services are found?
	}
	
	private boolean checkMethodParameterType(Method m) {
		var pts = m.getParameterTypes();
		if(pts == null || pts.length != 1) return false;
		var ptn = WebContext.class.getName();
		return ptn.equals(pts[0].getName());
	}	
}