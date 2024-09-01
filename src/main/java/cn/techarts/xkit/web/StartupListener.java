package cn.techarts.xkit.web;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import cn.techarts.xkit.ioc.Context;

public class StartupListener implements ServletContextListener {
	protected AppConfig config = null;
	
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		config = get(context, "webAppConfig", AppConfig.class);
		if(config == null) {
			throw new RuntimeException("App config is required.");
		}
		initSessionSettings(config); //Key, salt and permission-ignored
		this.loadAllWebServices(context, config.getServicePackage());
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}
	
	private void initSessionSettings(AppConfig settings) {
		var key = settings.getSessionKey();
		var salt = settings.getSessionSalt();
		var duration = settings.getSessionDuration();
		UserSession.init(salt, duration, key);
	}
	
	public<T> T get(ServletContext ctx, String id, Class<T> clzz)
	{
		return Context.from(ctx).get(id, clzz);
	}
	
	public Object get(ServletContext ctx, String id){
		return Context.from(ctx).get(id);
	}
	
	private void loadAllWebServices(ServletContext context, String pkg) {
		var services = scanWebServices(context, pkg);
		//There is not any service need to be exported
		if(services == null || services.isEmpty()) return;
		var container = Context.from(context);
		for(var service : services) {
			var exporter = container.get(service);
			if(exporter == null) continue;
			var methods = exporter.getClass().getMethods();
			if(methods == null || methods.length == 0) continue;
			for(var method : methods) {
				var ws = method.getAnnotation(WebMethod.class);
				//The method is not a (or not a legal) web service
				if(ws == null || ws.uri() == null) continue;
				if(!checkMethodParameterType(method)) continue;
				var s = new ServiceMeta(ws.uri(), exporter, method, ws.method());
				s.setPermissionRequired(ws.permission()); //Session
				if(!ServiceMeta.restful) {
					ServiceCache.cacheService(ws.uri(), s); // Without the prefix get|post 
				}else { //With a prefix string get|post such as get/user/login
					ServiceCache.cacheService(ws.uri(), ws.method(), s);
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
	
	/**
	 * Scan the specific package path to retrieve all exporters<p>
	 */
	private List<String> scanWebServices(ServletContext context, String pkg) {
		if(pkg == null) return null;
		var base = getApplicationBasePath();
		if(base == null) return null;
		var path = base.concat(pkg.replace('.', '/'));
		var files = poll(path, ".class");
		if(files == null || files.length == 0) return null;
		try {
			List<String> result = new ArrayList<>(24);
			for(var f : files) {
				if(f == null || !f.isFile()) continue;
				var n = f.getName().replace(".class", "");
				var c = Class.forName(pkg.concat(".").concat(n));
				if(c == null) continue;
				var named = c.getAnnotation(Named.class);
				var ws = c.getAnnotation(WebService.class);
				if(named == null || ws == null) continue;
				var name = named.value();
				if(name == null || name.isEmpty()) {
					name = c.getName();
				}
				result.add(name);
			}
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static File[] poll( String srcFolder, String fileType)
	{
		var directory = new File( srcFolder);
		return directory.listFiles( new XFileFilter(fileType));
	}
	
	private String getApplicationBasePath() {
		var base = getClass().getResource("/");
		if(base == null || base.getPath() == null) return null;
		var w = File.separatorChar == '\\'; //Windows
		return w ? base.getPath().substring(1) : base.getPath();
	}
}

class XFileFilter implements FileFilter
{
	private String type = null;
	
	public XFileFilter( String fileType)
	{
		this.type = fileType;
	}
	
	@Override
	public boolean accept( File file)
	{
		if( file == null) return false;
		if( this.type == null || this.type.isEmpty()) return true;
		return file.isFile() && file.getName().endsWith( this.type);
	}
}