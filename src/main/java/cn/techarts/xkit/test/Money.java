package cn.techarts.xkit.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import cn.techarts.xkit.ioc.Valued;

public class Money{
	private int integer;
	private int decemal;
	
	public Money() {
		this.integer = 50000;
		this.decemal = 500;
	}
	
	public int getInteger() {
		return integer;
	}
	public void setInteger(int integer) {
		this.integer = integer;
	}

	public int getDecemal() {
		return decemal;
	}
	public void setDecemal(int decemal) {
		this.decemal = decemal;
	}
}
