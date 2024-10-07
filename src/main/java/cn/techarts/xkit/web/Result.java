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

package cn.techarts.xkit.web;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author rocwon@gmail.com
 */
public class Result implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int code;
	private String text;
	private Object data;
	
	public Result() {}
	
	public Result(int code, String text) {
		this.code = code;
		this.text = text;
	}
	
	public Result(int code, String text, Object data) {
		this(code, text);
		this.data = data;
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

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}