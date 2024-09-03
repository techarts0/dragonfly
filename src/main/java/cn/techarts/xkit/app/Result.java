package cn.techarts.xkit.app;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class Result implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@JsonIgnore
	private int code;
	@JsonIgnore
	private String text;
	
	public Result() {}
	
	public Result(int code, String text) {
		this.code = code;
		this.text = text;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@JsonIgnore
	public boolean mark() {
		return this.code != 0;
	}
	public static Result ok() {
		return new Result(0, "OK");
	}
	
	public static Result unknown() {
		return new Result(-1, "Unknown exception");
	}
}