/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.dragonfly.app;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A super class of POJO (e.g database entity object).<p>
 * @author rocwon@gmail.com
 */
public class UniObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * In JPA, you should override the property in your sub-class 
	 * and declare it with the annotation @Id or @Column etc.
	 * 
	 */
	protected int id;
	
	private String name;
		
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
	 * Programmer could not use it in their code, so we provide a redundant property named "xid". 
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
	
	public int getPage() {
		return this.page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	@JsonIgnore
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
	
	//TODO for PseduQL
	public Map<String, Object> filter(String pql){
		return null;
	}
}