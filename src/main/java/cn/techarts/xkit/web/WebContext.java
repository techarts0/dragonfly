package cn.techarts.xkit.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.xkit.app.Codec;
import cn.techarts.xkit.app.Result;
import cn.techarts.xkit.app.UniObject;
import cn.techarts.xkit.util.Converter;


public class WebContext {
	
	private static final String CT_JSON = "application/json;charset=UTF-8";
	private static final String CT_FORM = "application/x-www-form-urlencoded";
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Result result = Result.ok();
	
	public WebContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
	
	public void respondAsJson(Object obj){
		if(obj == null) {
			if(!result.mark()) {
				result = Result.unknown();
			}
		}else if(obj instanceof UniObject){
			var tmp = (UniObject)obj;
			this.result = tmp.toResult();
		}else if(obj instanceof Result) {
			this.result = (Result)obj;
		}
		
		response.setContentType(CT_JSON);
		var content = Codec.toJson(obj);
		content = wrap(content, result);
		try{
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	public static void respondAsJson(HttpServletResponse response, Object result, int code, String msg){
		response.setContentType(CT_JSON);
		var content = Codec.toJson(result);
		content = wrap(content, new Result(code, msg));
		try{
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	protected static String wrap(String data, Result result) {
		return new StringBuilder(1024)
					 .append("{\"code\":")
			 		 .append(result.getCode())
					 .append(",\"text\":\"")
					 .append(result.getText())
					 .append("\",\"data\":")
					 .append(data)
					 .append("}").toString();
	}
	
	@Deprecated
	protected static Integer errno(Object content) {
		if(content == null) return Integer.valueOf(-1); //Failure
		if(!(content instanceof Integer)) return Integer.valueOf(0);
		Integer result = (Integer)content;
		return result.intValue() <= 0 ? result : Integer.valueOf(0);
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
		if(tmp == null) return List.of();
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
		if(p == null) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length == 16) p += ":00";
		if(length == 10) p += " 00:00:00";
		return Converter.toDate(p);
	}
	
	/**Get Date Parameter: yyyy/MM/dd*/
	public Date date(String name){
		var p = request.getParameter(name);
		if(p == null) return null;
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
		if(result == null) result = request.getHeader("X-Real-IP");
		return result == null ? request.getRemoteAddr() : result;
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
	 * An alias of the method {@link toBean}
	 */
	public<T> T fillBean(T bean) {
		return toBean(bean);
	}
	
	/**
	 * Fills the properties of java POJO with the request parameters automatically.<br>
	 * IMPORTANT: the property name MUST be same to the parameter name. And, <br>
	 * only the JDK built-in types and their object wrappers (e.g. long and Long)are supported.
	 */
	public<T> T toBean(T bean) {
		if(bean == null) return null;
		var clz = bean.getClass();
		var methods = clz.getMethods();
		if(methods == null) return bean;
		if(methods.length == 0) return bean;
		for(var m : methods) {
			var name = m.getName();
			if(!name.startsWith("set")) continue;
			var ps = m.getParameterTypes();
			if(ps == null || ps.length != 1) continue;
			var cs = name.substring(3).toCharArray();
			cs[0] += 32; //Convert the upper to lower
			setProperty(bean, m, ps[0],String.valueOf(cs));
		}
		return bean; //Return the POJO to support chain-calling style.
	}
	
	private void setProperty(Object pojo, Method m, Class<?> type, String name) {
		if(type == null) return;
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