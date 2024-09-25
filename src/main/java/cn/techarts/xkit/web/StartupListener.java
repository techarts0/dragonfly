package cn.techarts.xkit.web;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import cn.techarts.xkit.ioc.Context;
import cn.techarts.xkit.ioc.Panic;
import cn.techarts.xkit.util.Converter;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.Scanner;

public class StartupListener implements ServletContextListener {
	public static final String CONFIG_PATH = "contextConfigLocation";
	private static final Logger LOGGER = Hotpot.getLogger();
		
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var classpath = this.getRootClassPath();
		var config = getResourcePath("config.properties");
		var configs = Hotpot.resolveProperties(config);
		initializeIocContainer(context, classpath, configs);
		initSessionSettings(this.getSessionConfig(configs));
		var wsPackage = "web.service.package";//Scan the folder
		int n = loadServices(context, configs.remove(wsPackage));
		LOGGER.info("The web application is started (" + n + " web services)");
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
		if(result == null || result.getPath() == null){
			throw new Panic("Failed to get class path.");
		}
		return result.getPath();
	}
	
	private String getResourcePath(String resource) {
		var result = getClass().getResource("/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		result = getClass().getResource("/WEB-INF/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		throw new Panic("Failed to find the resource: [" + resource + "]");
	}
	
	private void initializeIocContainer(ServletContext context, String classpath, Map<String, String> configs) {
		Context.make(configs)
		       .cache(context)
		       .createFactory()
		       .scan(classpath)
		       .parse(getResourcePath("beans.xml"))
		       .register("cn.techarts.xkit.data.DatabaseFactory")
		       .register("cn.techarts.xkit.data.redis.RedisCacheHelper")
		       .start();
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
	
	private int loadServices(ServletContext context, String pkg) {
		var root = this.getRootClassPath();
		var scanner = new Scanner(root, pkg);
		var services = scanner.scanWebServices();
		//There is not any service need to be exported
		if(services == null || services.isEmpty()) return 0;
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
		return services.size(); //How many web-services are found?
	}
	
	private boolean checkMethodParameterType(Method m) {
		var pts = m.getParameterTypes();
		if(pts == null || pts.length != 1) return false;
		var ptn = WebContext.class.getName();
		return ptn.equals(pts[0].getName());
	}	
}