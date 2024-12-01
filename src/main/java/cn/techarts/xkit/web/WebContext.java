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

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.whale.Context;
import cn.techarts.xkit.util.Codec;
import cn.techarts.xkit.helper.Converter;

/**
 * @author rocwon@gmail.com
 */
public class WebContext {
	private List<String> arguments; //RESTFUL
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Result result = Result.ok();
	
	public WebContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
	
	public void setRestfulArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	
	public void respondAsJson(Object obj, MediaType type){
		if(obj != null) {
			this.result.setData(obj);
		}else {
			if(!result.mark()) {
				result = Result.unknown();
			}
		}
		response.setContentType(type.value());
		var content = Codec.toJson(result);
		try{
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			throw new RuntimeException("Failed to response.", e);
		}
	}
	
	public static void respondMessage(HttpServletResponse response, int code, String msg){
		var type = MediaType.JSON;
		response.setContentType(type.value());
		var info = new Result(code, msg);
		var content = Codec.toJson(info);
		try{
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			throw new RuntimeException("Failed to response.", e);
		}
	}
	
	public ServletContext getServletContext() {
		if(Objects.isNull(request)) return null;
		return this.request.getServletContext();
	}
	
	/**
	 * Retrieve managed object from DI container.<p>
	 * Null is returned if works stand-alonely.
	 */
	public<T> T get(String name, Class<T> clazz) {
		var ctx = getServletContext();
		if(Objects.isNull(ctx)) return null;
		var key = StartupListener.WHALE_KEY;
		var context = ctx.getAttribute(key);
		if(Objects.isNull(context)) return null;
		return ((Context)context).silent(name, clazz);
	}
	
	/**
	 * Retrieve managed object from DI container.<p>
	 * Null is returned if works stand-alonely.
	 */
	public<T> T get(Class<T> clazz) {
		return get(clazz.getName(), clazz);
	}
	
	/**Get path parameter in restful URL pattern:<br>
	 * For example: the request "/users/{id}/articles/{id}"<p>
	 * the index of first {id} is 0, and the second {id} is 1.<p>
	 * 
	 * Dragonfly does not support path parameter annotation mode.<br>
	 * In JSR370, the annotation is @PathParam, and<br>
	 * in Spring-MVC, the annotation is @PathVariable,<br>
	 * but they are not my taste, too tedious.
	 */
	public String get(int index) {
		if(Objects.isNull(arguments)) return null;
		var tmp = arguments.get(index);
		if(tmp != null) return tmp;
		var size = arguments.size() - 1;
		var bound = "[0 - " + size + "]";
		throw new RuntimeException("Index is out of boundary:" + bound);
	}
	
	/**
	 * @see get(int index)
	 */
	public int getInt(int index) {
		var tmp = get(index);
		if(Objects.isNull(tmp)) return 0;
		return Converter.toInt(tmp);
	}
	
	public float getFloat(int index) {
		var tmp = get(index);
		if(Objects.isNull(tmp)) return 0f;
		return Converter.toFloat(tmp);
	}
	
	public Date getDate(int index) {
		var tmp = get(index);
		if(Objects.isNull(tmp)) return null;
		return Converter.toDate(tmp);
	}
	
	public boolean getBool(int index) {
		var tmp = get(index);
		if(Objects.isNull(tmp)) return false;
		return Converter.toBoolean(tmp);
	}
	
	public int getInt(String name) {
		var val = request.getParameter(name);
		return Converter.toInt(val);
	}

	public long getlong(String name) {
		var val = request.getParameter(name);
		return Converter.toLong(val);
	}
	
	public String get(String name) {
		return request.getParameter(name);
	}
		
	public boolean getBool(String name) {
		var val = request.getParameter(name);
		return Converter.toBoolean(val);
	}
	
	public float getFloat(String name) {
		var val = request.getParameter(name);
		return Converter.toFloat(val);
	}
	
	public double getDouble(String name) {
		var val = request.getParameter(name);
		return Converter.toDouble(val);
	}
	
	public List<String> toList(String name, String separator){
		var tmp = request.getParameter(name);
		if(Objects.isNull(tmp)) return List.of();
		return Arrays.asList(tmp.split(separator));
	}
	
	public int uid() {
		return getInt("uid");
	}
	
	public int id() {
		return getInt("id");
	}
	
	public int xid() {
		return getInt("xid");
	}
	
	public String name() {
		return get("name");
	}
	
	public int page() {
		return getInt("page");
	}
	
	public int size() {
		return getInt("size");
	}
	
	/**Get Time Parameter: yyyy/MM/dd HH:mm:ss*/
	public Date time(String name){
		var p = request.getParameter(name);
		if(Objects.isNull(p)) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length == 16) p += ":00";
		if(length == 10) p += " 00:00:00";
		return Converter.toDate(p);
	}
	
	/**Get Date Parameter: yyyy/MM/dd*/
	public Date date(String name){
		var p = request.getParameter(name);
		if(Objects.isNull(p)) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length > 10) {
			p = p.substring(0, 10);
		}
		return Converter.toDate(p);
	}
	
	public void error(int code, String cause) {
		this.result = new Result(code, cause);
	}
	
	public static String getClientAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(Objects.isNull(result)) result = request.getHeader("X-Real-IP");
		return Objects.isNull(result) ? request.getRemoteAddr() : result;
	}
	
	public String ip() {
		return getClientAddress(this.request);
	}
	
	public HttpServletRequest getRequest() {
		return this.request;
	}
	
	public HttpServletResponse getResponse() {
		return this.response;
	}
	
	/**
	 * @return The value of user-agent field in HTTP Request header
	 */
	public String ua() {
		return request.getHeader("user-agent");
	}
	
	/**
	 * Return the parameter in HTTP header.
	 */
	public String head(String name) {
		return request.getHeader(name);
	}
	
	public String getSession(String userId) {
		var ctx = this.request.getServletContext();
		var obj = ctx.getAttribute(SessionConfig.CACHE_KEY);
		if(obj == null) return null; //ERROR
		var sessionConfig = (SessionConfig)obj;
		return sessionConfig.generate(ip(), userId);
	}
	
	public String getSession(int userId) {
		return this.getSession(Integer.toString(userId));
	}
	
	/**
	 * If the content-type of request is application/json, returns the whole content string. 
	 */
	public String getJson(){
		var ct = this.request.getContentType();
		if(!ct.toLowerCase().contains("/json")) return null;
		try {
			var bs = request.getInputStream().readAllBytes();
			return new String(bs, StandardCharsets.UTF_8);
		}catch(IOException e) {
			throw new RuntimeException("Failed to get json from request.", e);
		}
	}
	
	/**
	 * An alias of the method {@link toBbean}
	 */
	public<T> T bean(T bean) {
		return toBean(bean);
	}
	
	/**
	 * Fills the properties of java POJO with the request parameters automatically.<br>
	 * IMPORTANT: the property name MUST be same to the parameter name. And, <br>
	 * only the JDK built-in types and their object wrappers (e.g. long and Long)are supported.
	 */
	public<T> T toBean(T bean) {
		if(Objects.isNull(bean)) return null;
		var clz = bean.getClass();
		var methods = clz.getMethods();
		if(methods.length == 0) return bean;
		for(var m : methods) {
			var name = m.getName();
			if(!name.startsWith("set")) continue;
			var ps = m.getParameterTypes();
			if(ps.length != 1) continue;
			var cs = name.substring(3).toCharArray();
			cs[0] += 32; //Convert the upper to lower
			setProperty(bean, m, ps[0],String.valueOf(cs));
		}
		return bean; //Return the POJO to support chain-calling style.
	}
	
	private void setProperty(Object pojo, Method m, Class<?> type, String name) {
		if(Objects.isNull(type)) return;
		var typeName = type.getSimpleName();
		var val = request.getParameter(name);
		try {
			switch(typeName) {		
				case "String":
					m.invoke(pojo, val); break;
				case "Date":
					m.invoke(pojo, time(val)); break;
				case "int":
					m.invoke(pojo, Converter.toInt(val)); break;
				case "Integer":
					m.invoke(pojo, Integer.valueOf(Converter.toInt(val))); break;
				case "long":
					m.invoke(pojo, Converter.toLong(val)); break;
				case "Long":
					m.invoke(pojo, Long.valueOf(Converter.toLong(val))); break;
				case "double":
					m.invoke(pojo, Converter.toDouble(val)); break;
				case "Double":
					m.invoke(pojo, Double.valueOf(Converter.toDouble(val))); break;
				case "float":
					m.invoke(pojo, Converter.toFloat(val)); break;
				case "Float":
					m.invoke(pojo, Float.valueOf(Converter.toFloat(val))); break;
				case "Boolean":
					m.invoke(pojo, Converter.toBoolean(val)); break;
				default:
					//Ignored the unsupported parameter types.
			}
		}catch(Exception e){
			throw new RuntimeException("Failed to call the method: " + m.getName(), e);
		}
	}
}