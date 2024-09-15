package cn.techarts.xkit.web;

import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import cn.techarts.xkit.ioc.Context;
import cn.techarts.xkit.ioc.Panic;
import cn.techarts.xkit.util.Converter;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.PackageScanner;

public class StartupListener implements ServletContextListener {
	public static final String CONFIG_PATH = "contextConfigLocation";
		
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var classpath = this.getRootClassPath();
		var config = classpath.concat("config.properties");
		var configs = Hotpot.resolveProperties(config);
		initializeIocContainer(context, classpath, configs);
		initSessionSettings(this.getSessionConfig(configs));
		var wsPackage = "web.service.package";//Scan the folder
		loadAllWebServices(context, configs.remove(wsPackage));
	}
	
	private SessionConfig getSessionConfig(Map<String, String> configs) {
		var result = new SessionConfig();
		result.setSessionKey(configs.remove("session.key"));
		result.setSessionSalt(configs.remove("session.salt"));
		var duration = configs.remove("session.duration");		
		result.setSessionDuration(Converter.toInt(duration));
		var permission = configs.remove("session.check");
		result.setSessionCheck(Converter.toBoolean(permission));
		return result;
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(result == null || result.getPath() == null) {
			throw new Panic("Failed to get resource path.");
		}
		return result.getPath();
	}
	
	private void initializeIocContainer(ServletContext context, String classpath, Map<String, String> configs) {
		var json = classpath.concat("crafts.json");
		Context.make(classpath, json, configs).cache(context);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		var ctx = Context.from(arg0.getServletContext());
		if(ctx != null) ctx.close();
	}
	
	private void initSessionSettings(SessionConfig settings) {
		var key = settings.getSessionKey();
		var salt = settings.getSessionSalt();
		var check = settings.isSessionCheck();
		var duration = settings.getSessionDuration();
		UserSession.init(salt, duration, key, check);
	}
	
	public<T> T get(ServletContext ctx, String id, Class<T> clzz)
	{
		return Context.from(ctx).get(id, clzz);
	}
	
	public Object get(ServletContext ctx, String id){
		return Context.from(ctx).get(id);
	}
	
	private void loadAllWebServices(ServletContext context, String pkg) {
		var root = this.getRootClassPath();
		var scanner = new PackageScanner(root, pkg);
		var services = scanner.scanWebServices();
		//There is not any service need to be exported
		if(services == null || services.isEmpty()) return;
		var container = Context.from(context);
		for(var service : services) {
			var ws = container.get(service);
			if(ws == null) continue;
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
					ServiceCache.cacheService(wm.uri(), s); // Without the prefix get|post 
				}else { //With a prefix string get|post such as get/user/login
					ServiceCache.cacheService(wm.uri(), wm.method(), s);
				}
			}
		}
	}
	
	private boolean checkMethodParameterType(Method m) {
		var pts = m.getParameterTypes();
		if(pts == null || pts.length != 1) return false;
		var ptn = WebContext.class.getName();
		return ptn.equals(pts[0].getName());
	}	
}