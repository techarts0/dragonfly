package cn.techarts.xkit;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Generally, a POJO should derived from the class {@link IdObject} especially mapping on an entity in database.<p>
 * An instance of a subclass derived from {@link IdObject} owns a unique id(an integer) and supports serialization.<p>
 * The class contains below state properties:<br>
 * 1. error-code and error-cause<br>
 * 2. page-number and page-size
 */
public class IdObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String name;
	//Error Code
	@JsonIgnore
	private int code;
	//Cause Description
	@JsonIgnore
	private String text;
		
	//The page number
	@JsonIgnore
	private int page = 0;
	//Rows number in the page
	@JsonIgnore
	private int size = PAGESIZE;
		
	public static final int PAGESIZE = 20;
	public static final float ZERO = 0.00001f;
	public static final int INFINITE = 10000;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * In some languages such as OBJECT-C or PYTHON, the "id" is reserved as a keyword.
	 * Programmer could not use it in their codes, so we provide a redundant property named "xid". 
	 */
	public int getXid() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getText() {
		if(code == 0) {
			return "OK";
		}else {
			return text;
		}
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public int getPage() {
		return this.page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}

	public int getOffset() {
		return page * size;
	}
	
	/**
	 * How many rows are returned in this batch?
	 */
	public int getSize() {
		return size <= 0 ? PAGESIZE : size;
	}
	
	/**
	 * How many rows are returned in this batch?
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	@JsonIgnore
	public Result toResult() {
		return new Result(code, text);
	}
}