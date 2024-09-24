package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import cn.techarts.xkit.ioc.Valued;

@Singleton
public class Person {
	@Inject
	@Valued(key="user.id")
	private int id;
	private String name;
	
	private Mobile mobile;
	
	private Provider<Office> office;
	
	@Inject
	public Person(Provider<Mobile> mobile) {
		this.mobile = mobile.get();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Mobile getMobile() {
		return mobile;
	}
	
	public Office getOffice() {
		return office.get();
	}
}
