package cn.techarts.xkit.ioc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContextListener implements ServletContextListener {
	
	public static final String CONFIG_PATH = "contextConfigLocation";
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		var res = getClass().getResource("/");
		if(res == null || res.getPath() == null) {
			throw new Panic("Failed to get resource path.");
		}
		var classpath = res.getPath(); //WEB-INF/classes
		var json = classpath.concat("crafts.json");
		var config = classpath.concat("config.properties");
		var context = sce.getServletContext();
		Context.make(classpath, json, config).cache(context);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
}