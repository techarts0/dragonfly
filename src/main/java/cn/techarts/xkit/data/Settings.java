package cn.techarts.xkit.data;

import javax.inject.Inject;

import cn.techarts.whale.Valued;

public class Settings {
	@Inject
	@Valued(key="jdbc.url")
	protected String url;
	
	@Inject
	@Valued(key="jdbc.username")
	protected String user;
	
	@Inject
	@Valued(key="jdbc.driver")
	protected String driver;
	
	@Inject
	@Valued(key="jdbc.password")
	protected String password;
	
	@Inject
	@Valued(key="jdbc.capacity")
	protected int capacity;
	
	@Inject
	@Valued(key="jdbc.framework")
	protected String framework;
	
	//For MyBatis and OPENJPA
	@Inject
	@Valued(key="jdbc.model.package")
	protected String modelPackage;
}