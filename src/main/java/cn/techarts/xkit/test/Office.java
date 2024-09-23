package cn.techarts.xkit.test;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Office {
	private int id;
	private String building;
	private Provider<Mobile> mobile;
	
	public Office() {}
	
	public Mobile getMobile() {
		return mobile.get();
	}
}
