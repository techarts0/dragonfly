package cn.techarts.xkit.test;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Office {
	private int id = 22;
	private String building;
	private Provider<Mobile> mobile;
	
	public Office() {}
	
	public Mobile getMobile() {
		return mobile.get();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
