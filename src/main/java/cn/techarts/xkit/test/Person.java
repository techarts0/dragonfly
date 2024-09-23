package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Person {
	private int id;
	private String name;
	
	private Mobile mobile;
	
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
}
