package cn.techarts.xkit.ioc;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import cn.techarts.xkit.util.Hotpot;

public class WebListener implements ServletContextListener {
		
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var classpath = this.getRootClassPath();
		var xmlBeans = getCraftConfiguration();
		var config = classpath.concat("config.properties");
		var configs = Hotpot.resolveProperties(config);
		Context.make(classpath, xmlBeans, configs).cache(context);
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(result == null || result.getPath() == null) {
			throw new Panic("Failed to get resource path.");
		}
		return result.getPath();
	}
	
	private String getCraftConfiguration() {
		var res = getClass().getResource("/beans.xml");
		if(res == null || res.getPath() == null) {
			throw new Panic("Failed to get resource path.");
		}
		return res.getPath();
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
}