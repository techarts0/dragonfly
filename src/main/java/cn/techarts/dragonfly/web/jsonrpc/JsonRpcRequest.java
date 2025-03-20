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
package cn.techarts.dragonfly.web.jsonrpc;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import cn.techarts.dragonfly.app.helper.Empty;
import cn.techarts.dragonfly.app.helper.Slicer;

/**
 * @author rocwon@gmail.com
 */
public class JsonRpcRequest extends JsonRpcMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	private String method;
	private Map<String, Object> params;
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	/*
	 * Blank is allowed
	 */
	public boolean hasMethod() {
		if(method == null) return false;
		return !"".equals(method);
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
	
	public JsonRpcError toError() {
		return new JsonRpcError(getId());
	}
	
	public JsonRpcResult toResult(Object obj) {
		return new JsonRpcResult(getId(), obj);
	}
	
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	private<T> T get(String name, Class<T> t) {
		if(params == null) return null;
		var tmp = params.get(name);
		if(!t.isInstance(tmp)) {
			return null;
		}else {
			return t.cast(tmp);
		}
	}
	
	public int getInt(String name) {
		var tmp = get(name, Integer.class);
		return tmp != null ? tmp.intValue() : 0;
	}
	
	public long getLong(String name) {
		var tmp = get(name, Long.class);
		return tmp != null ? tmp.longValue() : 0;
	}
	
	public float getFloat(String name) {
		var tmp = get(name, Float.class);
		return tmp != null ? tmp.floatValue() : 0;
	}
	
	public double getDouble(String name) {
		var tmp = get(name, Double.class);
		return tmp != null ? tmp.doubleValue() : 0;
	}
	
	public String get(String name) {
		return get(name, String.class);
	}
	
	public Date getDate(String name) {
		return get(name, Date.class);
	}
	
	public boolean getBool(String name) {
		var tmp = get(name, Boolean.class);
		return tmp != null ? tmp.booleanValue() : false;
	}
	
	public<T> T toBean(T bean) {
		if(Empty.is(params)) return null;
		try {
			var clazz = bean.getClass();
			var methods = clazz.getMethods();
			if(Empty.is(methods)) return bean;
			for(var m : methods) {
				var name = m.getName();
				if(!name.startsWith("set")) continue;
				if(m.getParameterCount() != 1) continue;
				var param = params.get(toFieldName(name));
				if(param != null) m.invoke(bean, param);
			}
			return bean;
		}catch(Exception e) {
			var name = bean.getClass().getName();
			throw new RuntimeException("Failed to convert params to " + name, e);
		}	
	}
	
	private String toFieldName(String method) {
		var chars = method.toCharArray();
		var idx = method.startsWith("is") ? 2 : 3;
		chars[idx] = (char)(chars[idx] + 32); //To lower-case
		return new String(Slicer.slice(chars, idx, 100));
	}
}