package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import cn.techarts.xkit.ioc.Valued;

@Singleton
public class Office {
	private int id = 22;
	private String building;
	private Mobile mobile;
	private Provider<Person> admin;
	
	public Office() {}
	
	public Mobile getMobile() {
		return mobile;
	}
	
	@Inject
	public void setMobile(Provider<Mobile> mobile) {
		this.mobile = mobile.get();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBuilding() {
		return building;
	}

	@Inject
	public void setBuilding(@Valued(key="build.name") String building) {
		this.building = building;
	}

	public Person getAdmin() {
		return admin.get();
	}

}
