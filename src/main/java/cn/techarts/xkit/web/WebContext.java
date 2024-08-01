package cn.techarts.xkit.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.techarts.jhelper.Codec;
import cn.techarts.jhelper.Time;
import cn.techarts.jhelper.Converter;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Spliter;

public class WebContext {
	
	private static final String CT_JSON = "application/json;charset=UTF-8";
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String code = "0", text = "OK";
	
	public WebContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
	
	public void respondAsJson(Object result){
		try{
			response.setContentType(CT_JSON);
			var content = Codec.toJson(result);
			content = std(content, code, text);
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	public static void respondAsJson(HttpServletResponse response, Object result, int code, String msg){
		try{
			var err = Integer.toString(code);
			response.setContentType(CT_JSON);
			var content = std(null, err, msg);
			response.getWriter().write(content);
			response.getWriter().flush();
		}catch(IOException e){ 
			e.printStackTrace();
		}
	}
	
	protected static String std(String data, String code, String message) {
		return new StringBuilder(1024)
					 .append("{\"code\":")
			 		 .append(code)
					 .append(",\"text\":\"")
					 .append(message)
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
	
	public List<String> toList(String name, char separator){
		var tmp = request.getParameter(name);
		if(tmp == null) return Empty.list();
		return Spliter.split(tmp, separator);
	}
	
	public int uid() {
		return getInt("uid");
	}
	
	public int id() {
		return getInt("id");
	}
	
	public String name() {
		return get("name");
	}
	
	public int offset() {
		return getInt("offset");
	}
	
	/**Get Time Parameter: yyyy/MM/dd HH:mm:ss*/
	public Date time(String name){
		var p = request.getParameter(name);
		if(p == null) return null;
		int length = p.length();
		if(length < 10) return null;
		if(length == 16) p += ":00";
		if(length == 10) p += " 00:00:00";
		return Time.parse(p);
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
		return Time.parse(p);
	}
	
	public void error(int code, String text) {
		this.text = text;
		this.code = Integer.toString(code);
	}
	
	public static String getRemorteAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(result == null) result = request.getHeader("X-Real-IP");
		return result == null ? request.getRemoteAddr() : result;
	} 
	
	public HttpServletRequest getRequest() {
		return this.request;
	}
	
	public HttpServletResponse getResponse() {
		return this.response;
	}
	
	/**
	 * Fill the POJO with the request parameters automatically.<br>
	 * IMPORTANT: the property name MUST be same to the parameter name.
	 */
	public<T> T toPojo(T pojo) {
		if(pojo == null) return null;
		var clz = pojo.getClass();
		var methods = clz.getMethods();
		if(methods == null) return pojo;
		if(methods.length == 0) return pojo;
		for(var m : methods) {
			var name = m.getName();
			if(!name.startsWith("set")) continue;
			var ps = m.getParameterTypes();
			if(ps == null || ps.length != 1) continue;
			var cs = name.substring(3).toCharArray();
			cs[0] += 32; //Convert the upper to lower
			setProperty(pojo, m, ps[0],String.valueOf(cs));
		}
		return pojo; //Return the POJO to support chain-calling style.
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
			e.printStackTrace();
		}
	}
}