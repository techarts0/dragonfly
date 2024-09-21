package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import cn.techarts.xkit.ioc.Valued;

@Named
public class Mobile {
	private String zone;
	private String number;
	
	@Inject
	public Mobile(@Valued(key="zone") String zone, @Valued(val="13980092699")String number) {
		this.zone = zone;
		this.number = number;
	}
	
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
}
