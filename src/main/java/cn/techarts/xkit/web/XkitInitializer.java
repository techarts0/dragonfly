package cn.techarts.xkit.web;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import cn.techarts.xkit.app.ServiceEnhancer;
import cn.techarts.xkit.ioc.Panic;

public class XkitInitializer implements ServletContainerInitializer{
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		var classpath = this.getRootClassPath();
		new ServiceEnhancer(classpath).start();
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(result == null || result.getPath() == null) {
			throw new Panic("Failed to get resource path.");
		}
		return result.getPath(); //Usually, it is WEB-INF/classes
	}
}