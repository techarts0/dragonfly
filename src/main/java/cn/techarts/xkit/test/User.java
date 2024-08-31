package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import cn.techarts.xkit.ioc.Valued;

public class User{
	private int id;
	
	
	private Money salary;
	
	public User() {
		this.id = 5;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Money getSalary() {
		return this.salary;
	}
	
}
