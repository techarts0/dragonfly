package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import cn.techarts.xkit.ioc.Valued;

@Named("Person")
@Singleton
public class Person{
	
	//@Inject
	//@Valued(val="Xiao Zhong")
	private String name;
	private User contact;
	
	//@Inject
	public Person(@Named("User") User user) {
		this.contact = user;
	}
	
	@Inject
	public Person(@Named("User")User user, @Valued(val="Xiao Zhong")String name) {
		this.contact = user;
		this.name = name;
	}
	
	
	public Person(User contact, int age) {
		this.contact = contact;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public User getContact() {
		return contact;
	}
	public void setContact(User contact) {
		this.contact = contact;
	}
	
}
