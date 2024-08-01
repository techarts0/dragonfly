package cn.techarts.xkit.web;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import cn.techarts.jhelper.Cacher;
import cn.techarts.jhelper.Empty;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebListener
public class StartupListener implements ServletContextListener {
	protected ServiceConfig config = null;
	
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		config = get(context, "serviceConfig", ServiceConfig.class);
		if(config == null) {
			throw new RuntimeException("Fatal Error: Service settings is required!");
		}
		initSessionSettings(config); //Key, salt and permission-ignored
		this.loadAllServiceExporters(context, config.getExporterPackage());
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		Cacher.destroy();
	}
	
	private void initSessionSettings(ServiceConfig settings) {
		var key = settings.getSessionKey();
		var salt = settings.getSessionSalt();
		var duration = settings.getSessionDuration();
		UserSession.init(salt, duration, key);
	}
	
	/**
	 * @return Returns an object cached in spring IOC container.
	 */
	public<T> T get(ServletContext ctx, String id, Class<T> clzz)
	{
		if(ctx == null) return null;
		var context = WebApplicationContextUtils
				.getWebApplicationContext( ctx);
		try{
			if( context == null) return null;
			return context.getBean( id, clzz);
		}catch( Exception e){
			return null;
		}
	}
	
	/**
	 * @return Returns an object cached in spring IOC container.
	 */
	public Object get(ServletContext ctx, String id)
	{
		if(ctx == null) return null;
		var context = WebApplicationContextUtils
				.getWebApplicationContext( ctx);
		try{
			return context != null ? context.getBean(id) : null;
		}catch( Exception e){
			return null;
		}
	}
	
	private void loadAllServiceExporters(ServletContext context, String pkg) {
		var services = scanServiceExporters(context, pkg);
		//There is not any service need to be exported
		if(services == null || services.isEmpty()) return;
		for(var service : services) {
			var exporter = get(context, service);
			if(exporter == null) continue;
			var methods = exporter.getClass().getMethods();
			if(methods == null || methods.length == 0) continue;
			for(var method : methods) {
				var ws = AnnotationUtils.findAnnotation(method, WebService.class);
				//The method is not a (or not a legal) web service
				if(ws == null || ws.uri() == null) continue;
				if(!checkMethodParameterType(method)) continue;
				var s = new ServiceMeta(ws.uri(), exporter, method, ws.method());
				s.setSessionRequired(ws.sessionRequired()); //Session
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
	 * @TODO Now it can't process the sub-packages, we need to improve it later.
	 */
	private List<String> scanServiceExporters(ServletContext context, String pkg) {
		if(Empty.is(pkg)) return null;
		var base = getApplicationBasePath();
		if(Empty.is(base)) return null;
		var path = base.concat(pkg.replace('.', '/'));
		var files = poll(path, ".class");
		if(Empty.is(files)) return null;
		try {
			List<String> result = new ArrayList<>(24);
			for(var f : files) {
				if(f == null || !f.isFile()) continue;
				var n = f.getName().replace(".class", "");
				var c = Class.forName(pkg.concat(".").concat(n));
				if(c == null) continue;
				var a = c.getAnnotation(ServiceExporter.class);
				if(a != null && a.value() != null) result.add(a.value());
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
		return directory.listFiles( new XFileFilter( fileType));
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