package cn.techarts.xkit.ioc;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContextListener implements ServletContextListener {
	
	public static final String CONFIG_PATH = "contextConfigLocation";
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		var context = sce.getServletContext();
		var json = getJsonFilePath(context);
		var config = getConfigFilePath(context);
		var classpath = getApplicationBasePath();
		Context.make(classpath, json, config).cache(context);
	}
	
	private String getApplicationBasePath() {
		var base = getClass().getResource("/");
		if(base == null) return null;
		var w = File.separatorChar == '\\'; //Windows
		return w ? base.getPath().substring(1) : base.getPath();
	}
	
	private String getJsonFilePath(ServletContext context) {
		var path =  context.getRealPath("/"); //Root of the application
		return path + "WEB-INF" + File.separator + "crafts.json";
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
	
	private String getConfigFilePath(ServletContext context) {
		var file = context.getInitParameter(CONFIG_PATH);
		if(file == null || "".equals(file.trim())) {
			file = "WEB-INF" + File.separator + "config.properties";
		}
		return context.getRealPath("/").concat(file);
	}
}