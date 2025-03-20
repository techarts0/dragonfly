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

package cn.techarts.dragonfly.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.dragonfly.app.helper.Converter;
import cn.techarts.dragonfly.util.Codec;
import cn.techarts.dragonfly.web.jsonrpc.JsonRpcError;
import cn.techarts.dragonfly.web.jsonrpc.JsonRpcRequest;
import cn.techarts.dragonfly.web.jsonrpc.JsonRpcResult;
import cn.techarts.dragonfly.web.token.ClientContext;
import cn.techarts.dragonfly.web.token.TokenConfig;
import cn.techarts.whale.Context;

/**
 * @author rocwon@gmail.com
 */
public class WebContext {
	//Restful path parameters
	private List<String> arguments;
	private Result result = Result.ok();
	private JsonRpcError  resulte = null;
	private JsonRpcResult resultj = null;
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private JsonRpcRequest jsonRpcRequest;
	private jakarta.servlet.http.HttpServletRequest request0;
	private jakarta.servlet.http.HttpServletResponse response0;
	
	//The constant MUST be same as Context.NAME in whale project.
	private static final String WHALE_KEY = "context.whale.techarts";

	public WebContext(HttpServletRequest request, HttpServletResponse response, boolean jsonrpc) {
		this.request = request;
		this.response = response;
		this.parseJsonRpc(jsonrpc);
	}
	
	/**
	 * For Jakarta Servlet API
	 */
	public WebContext(jakarta.servlet.http.HttpServletRequest request, 
					  jakarta.servlet.http.HttpServletResponse response, 
					  boolean jsonrpc) {
		this.request0 = request;
		this.response0 = response;
		this.parseJsonRpc(jsonrpc);
	}
	
	private void parseJsonRpc(boolean jsonrpc) {
		if(!jsonrpc) return;
		try {
			jsonRpcRequest = getJson(JsonRpcRequest.class);
			if(jsonRpcRequest == null) {
				jsonRpcRequest = new JsonRpcRequest(); //Empty
				error(JsonRpcError.INVALID_REQUEST, "Invalid request");
			}else if(!jsonRpcRequest.hasMethod()) {
				error(JsonRpcError.METHOD_NOT_FOUND, "Method not found");
			}
		}catch(RuntimeException e) {
			this.error(JsonRpcError.PARSE_ERROR, "Parse error");
		}
		//Respond directly if a known error occurred on parsing.
		if(this.resulte != null) this.respondAsJson(null);
	}
	
	/**
	 * @return Returns true if an error has occurred in constructor.
	 */
	public boolean hasError() {
		return this.resulte != null;
	}
	
	public void setPathParameters(List<String> arguments) {
		this.arguments = arguments;
	}
	
	public void respondAsBinary(byte[] bin, MediaType type) {
		if(bin == null || bin.length == 0) return;
		this.responds(type.value(), bin);
	}
	
	public void respondAsText(String content, MediaType type) {
		this.responds(type.value(), content);
	}
	
	public void respondAsJson(Object data) {
		String result = null;
		if(jsonRpcRequest == null) {
			result = this.serializeDefault(data);
		}else {
			result = this.serializeJsonRpc(data);
		}
		responds(MediaType.JSON.value(), result);
	}
	
	private String serializeDefault(Object obj){
		if(obj == null) {
			if(!result.mark()) {
				result = Result.unknown();
			}
		}else if(obj instanceof Result) {
			this.result = (Result)obj;
		}else {
			this.result.setData(obj);
		}		
		return Codec.toJson(this.result);
	}
	
	private String serializeJsonRpc(Object obj) {
		if(resulte != null) {
			resulte.setErrorData(obj);
			return Codec.toJson(this.resulte);
		}else {
			resultj = jsonRpcRequest.toResult(obj);
			return Codec.toJson(this.resultj);
		}
	}
	
	//Javax & Jakarta API 
	private void responds(String contentType, String content) {
		try {
		if(this.response != null) { //Javax
			response.setContentType(contentType);
			response.getWriter().write(content);
			response.getWriter().flush();
		}else {	//Jakarta
			response0.setContentType(contentType);
			response0.getWriter().write(content);
			response0.getWriter().flush();
		}
		}catch(IOException e) {
			throw new RuntimeException("Failed to respond.", e);
		}
	}
	
	//Javax & Jakarta API 
		private void responds(String contentType, byte[] content) {
			try {
			if(this.response != null) { //Javax
				response.setContentType(contentType);
				response.getOutputStream().write(content);
				response.getOutputStream().flush();
			}else {	//Jakarta
				response0.setContentType(contentType);
				response0.getOutputStream().write(content);
				response0.getOutputStream().flush();
			}
			}catch(IOException e) {
				throw new RuntimeException("Failed to respond.", e);
			}
		}
	
	private String getParameter(String name) {
		if(request != null) { //Javax
			return request.getParameter(name);
		}else {	//Jakarta
			return request0.getParameter(name);
		}
	}
	
	public Object getAttribute(String name) {
		if(request != null) { //Javax
			var ctx = request.getServletContext();
			if(Objects.isNull(ctx)) return null;
			return ctx.getAttribute(name);
		}else {	//Jakarta
			var ctx = request0.getServletContext();
			if(Objects.isNull(ctx)) return null;
			return ctx.getAttribute(name);
		}
	}
	
	public void setAttribute(String name, Object value) {
		if(request != null) { //Javax
			var ctx = request.getServletContext();
			if(Objects.isNull(ctx)) return;
			ctx.setAttribute(name, value);
		}else {	//Jakarta
			var ctx = request0.getServletContext();
			if(Objects.isNull(ctx)) return;
			ctx.setAttribute(name, value);
		}
	}
	
	/**
	 * Return the parameter in HTTP header.
	 */
	public String getHeader(String name) {
		if(request != null) { //Javax
			return request.getHeader(name);
		}else {	//Jakarta
			return request0.getHeader(name);
		}
	}
	
	public void setHeader(String name, String value) {
		if(response != null) { //Javax
			response.setHeader(name, value);
		}else {	//Jakarta
			response0.setHeader(name, value);
		}
	}
	
	private String getContentType() {
		if(request != null) { //Javax
			return request.getContentType();
		}else {	//Jakarta
			return request0.getContentType();
		}
	}
	
	private byte[] readRequestBytes() {
		try {
		if(request != null) { //Javax
			return request.getInputStream().readAllBytes();
		}else {	//Jakarta
			return request0.getInputStream().readAllBytes();
		}
		}catch(IOException e) {
			throw new RuntimeException("Failed to read json from request.", e);
		}
	}
	
	public static void respondMessage(HttpServletResponse response, int code, String msg){
		var type = MediaType.JSON;
		var info = new Result(code, msg);
		var content = Codec.toJson(info);
		try{
			response.setContentType(type.value());
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			throw new RuntimeException("Failed to respond.", e);
		}
	}
	
	/**
	 * For Jakarta Servlet API
	 */
	public static void respondMessage(jakarta.servlet.http.HttpServletResponse response, int code, String msg){
		var type = MediaType.JSON;
		var info = new Result(code, msg);
		var content = Codec.toJson(info);
		try{
			response.setContentType(type.value());
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			throw new RuntimeException("Failed to respond.", e);
		}
	}

	/**
	 * Retrieve managed object from DI container.<p>
	 * Null is returned if works stand-alonely.
	 */
	public<T> T get(String name, Class<T> clazz) {
		var context = getAttribute(WHALE_KEY);
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
	 * For example:<br> the request "/user/{id}/articles/{id}"<p>
	 * The index of path parameter is from 0 like an array in C or Java. 
	 * So the index of first {id} is 0, and the second {id} is 1.<p>
	 * Dragonfly does not support path parameter annotation mode.<br>
	 * In JSR370, the annotation is @PathParam, and<br>
	 * in Spring-MVC, the annotation is @PathVariable,<br>
	 * but they are not my taste, too tedious.
	 * 
	 * @parama index A number from 0 to (n - 1).
	 */
	public String at(int index) {
		if(Objects.isNull(arguments)) return null;
		var tmp = arguments.get(index);
		if(tmp != null) return tmp;
		var size = arguments.size() - 1;
		var bound = "[0 - " + size + "]";
		throw new RuntimeException("The path parameter index is out of range:" + bound);
	}
	
	/**
	 * @see get(int index)
	 */
	public int intAt(int index) {
		var tmp = at(index);
		if(Objects.isNull(tmp)) return 0;
		return Converter.toInt(tmp);
	}
	
	public float floatAt(int index) {
		var tmp = at(index);
		if(Objects.isNull(tmp)) return 0f;
		return Converter.toFloat(tmp);
	}
	
	public Date dateAt(int index) {
		var tmp = at(index);
		if(Objects.isNull(tmp)) return null;
		return Converter.toDate(tmp);
	}
	
	public boolean boolAt(int index) {
		var tmp = at(index);
		if(Objects.isNull(tmp)) return false;
		return Converter.toBoolean(tmp);
	}
	
	public int getInt(String name) {
		if(jsonRpcRequest == null) {
			var val = getParameter(name);
			return Converter.toInt(val);
		}else {			
			return jsonRpcRequest.getInt(name);
		}
	}

	public long getlong(String name) {
		if(jsonRpcRequest == null) {
			var val = getParameter(name);
			return Converter.toLong(val);
		}else {
			return jsonRpcRequest.getLong(name);
		}
	}
	
	public String get(String name) {
		if(jsonRpcRequest == null) {
			return getParameter(name);
		}else {
			return jsonRpcRequest.get(name);
		}
	}
		
	public boolean getBool(String name) {
		if(jsonRpcRequest == null) {
		var val = getParameter(name);
		return Converter.toBoolean(val);
		}else {
			return jsonRpcRequest.getBool(name);
		}
	}
	
	public float getFloat(String name) {
		if(jsonRpcRequest == null) {
			var val = getParameter(name);
			return Converter.toFloat(val);
		}else {
			return jsonRpcRequest.getFloat(name);
		}
	}
	
	public double getDouble(String name) {
		if(jsonRpcRequest == null) {
			var val = getParameter(name);
			return Converter.toDouble(val);
		}else {
			return jsonRpcRequest.getDouble(name);
		}
	}
	
	public List<String> toList(String name, String separator){
		var tmp = getParameter(name);
		if(Objects.isNull(tmp)) return List.of();
		return Arrays.asList(tmp.split(separator));
	}
	
	public int page() {
		return getInt("page");
	}
	
	public int size() {
		return getInt("size");
	}
	
	/**Get Time Parameter: yyyy/MM/dd HH:mm:ss*/
	public Date time(String name){
		var p = getParameter(name);
		if(Objects.isNull(p)) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length == 16) p += ":00";
		if(length == 10) p += " 00:00:00";
		return Converter.toDate(p);
	}
	
	/**Get Date Parameter: yyyy/MM/dd*/
	public Date date(String name){
		var p = getParameter(name);
		if(Objects.isNull(p)) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length > 10) {
			p = p.substring(0, 10);
		}
		return Converter.toDate(p);
	}
	
	/**JSON RPC method property*/
	public String getMethod() {
		if(jsonRpcRequest == null) {
			return null;
		}else {
			return jsonRpcRequest.getMethod();
		}
	}
	
	/**JSON RPC id property*/
	public int getId() {
		return jsonRpcRequest.getId();
	}
	
	public void error(int code, String cause) {
		if(this.jsonRpcRequest == null) {
			this.result = new Result(code, cause);
		}else {
			resulte = jsonRpcRequest.toError();
			resulte.error(code, cause);
		}
	}
	
	public static String getClientAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(Objects.isNull(result)) result = request.getHeader("X-Real-IP");
		return Objects.isNull(result) ? request.getRemoteAddr() : result;
	}
	
	public static String getClientAddress(jakarta.servlet.http.HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(Objects.isNull(result)) result = request.getHeader("X-Real-IP");
		return Objects.isNull(result) ? request.getRemoteAddr() : result;
	}
	
	public String ip() {
		if(request != null) {
			return getClientAddress(request);
		}else {
			return getClientAddress(request0);
		}
	}
	
	/**
	 * @return The value of user-agent field in HTTP Request header
	 */
	public String userAgent() {
		return getHeader("user-agent");
	}
	
	public String getToken(String userId) {
		var obj = getAttribute(TokenConfig.CACHE_KEY);
		if(obj == null) return null; //ERROR
		var sessionConfig = (TokenConfig)obj;
		var client = new ClientContext(ip(), userAgent(), userId);
		return sessionConfig.getTokenizer().create(client, sessionConfig);
	}
	
	public String getToken(int userId) {
		return this.getToken(Integer.toString(userId));
	}
	
	/**
	 * The content-type of request is application/json 
	 */
	public<T> T getJson(Class<T> t){
		var ct = getContentType();
		if(!ct.toLowerCase().contains("/json")) {
			throw new RuntimeException("The content type is not JSON.");
		}
		var bytes = this.readRequestBytes();
		if(bytes == null || bytes.length == 0) return null;
		return Codec.decodeJson(new String(bytes, StandardCharsets.UTF_8), t);
	}
	
	/**
	 * Fills the properties of java POJO with the request parameters automatically.<br>
	 * IMPORTANT: the property name MUST be same to the parameter name. And, <br>
	 * only the JDK built-in types and their object wrappers (e.g. long and Long)are supported.
	 */
	public<T> T getBean(T bean) {
		if(Objects.isNull(bean)) return null;
		if(jsonRpcRequest != null) {
			return jsonRpcRequest.toBean(bean);
		}
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
			setProperty(bean, m, ps[0], getParameter(String.valueOf(cs)));
		}
		return bean; //Return the POJO to support chain-calling style.
	}
	
	private static void setProperty(Object pojo, Method m, Class<?> type, String val) {
		if(Objects.isNull(type)) return;
		var typeName = type.getSimpleName();
		try {
			switch(typeName) {		
				case "String":
					m.invoke(pojo, val); break;
				case "Date":
					m.invoke(pojo, Converter.toDate(val)); break; //time
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