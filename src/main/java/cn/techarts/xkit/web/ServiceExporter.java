package cn.techarts.xkit.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExporter {
	/**
	 * The property value is same to the object name in SPRING-IOC
	 */
	public String value();
	
}
