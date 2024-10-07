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

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.techarts.xkit.web.restful.Delete;
import cn.techarts.xkit.web.restful.Get;
import cn.techarts.xkit.web.restful.Head;
import cn.techarts.xkit.web.restful.Post;
import cn.techarts.xkit.web.restful.Put;

/**
 * @author rocwon@gmail.com
 */
public final class ServiceMeta {
	private Object object;
	private Method method;
	private String uri = null;
	private String httpMethod;
	private boolean restful = false;
	private boolean permission = true;
	
	private List<String> arguments;
	
	public ServiceMeta(Get m, Object obj, Method method) {
		this.httpMethod= "GET";
		setAttrs(obj, method, m.value(), m.permission());
	}
	
	public ServiceMeta(Post m, Object obj, Method method) {
		this.httpMethod= "POST";
		setAttrs(obj, method, m.value(), m.permission());
	}
	
	public ServiceMeta(Put m, Object obj, Method method) {
		this.httpMethod= "PUT";
		setAttrs(obj, method, m.value(), m.permission());
	}
	
	public ServiceMeta(Delete m, Object obj, Method method) {
		this.httpMethod= "DELETE";
		setAttrs(obj, method, m.value(), m.permission());
	}
	
	public ServiceMeta(Head m, Object obj, Method method) {
		this.httpMethod= "HEAD";
		setAttrs(obj, method, m.value(), m.permission());
	}
	
	public ServiceMeta(WebMethod m, Object obj, Method method) {
		this.httpMethod= m.method().toUpperCase();
		setAttrs(obj, method, m.uri(), m.permission());
		this.restful = m.restful(); //Recover the default value.
	}	
	
	private void setAttrs(Object object, Method method, String uri, boolean permission) {
		this.uri = uri;
		this.restful = true;
		this.object = object;
		this.method = method;
		this.permission = permission;
	}	
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public void call(HttpServletRequest request, HttpServletResponse response) {
		if(method == null || object == null) return;
		try {
			var p = new WebContext(request, response);
			p.setRestfulArguments(this.arguments);
			p.respondAsJson(method.invoke(object, p));
		} catch (Exception e) {
			throw new RuntimeException("Failed to execute the web service.",  e);
		}
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}	

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public boolean isPermissionRequired() {
		return permission;
	}

	public void setPermissionRequired(boolean permissionRequired) {
		this.permission = permissionRequired;
	}
	
	public ServiceMeta setArguments(List<String> arguments) {
		this.arguments = arguments;
		return this;
	}
	
	public List<String> getArguments(){
		return this.arguments;
	}

	public boolean isRestful() {
		return restful;
	}

	public void setRestful(boolean restful) {
		this.restful = restful;
	}
	
	public String getConcreteUri() {
		if(!restful) return this.uri;
		return httpMethod.concat(uri);
	}
	
	public static ServiceMeta to(Method method, Object target) {
		var result = method.getDeclaredAnnotations();
		if(result == null || result.length == 0) return null;
		for(var annotation : result) {
			if(annotation instanceof WebMethod) {
				var tmp = (WebMethod)annotation;
				return new ServiceMeta(tmp, target, method);
			}else if(annotation instanceof Get) {
				var tmp = (Get)annotation;
				return new ServiceMeta(tmp, target, method);
			}else if(annotation instanceof Post) {
				var tmp = (Post)annotation;
				return new ServiceMeta(tmp, target, method);
			}else if(annotation instanceof Put) {
				var tmp = (Put)annotation;
				return new ServiceMeta(tmp, target, method);
			}else if(annotation instanceof Delete) {
				var tmp = (Delete)annotation;
				return new ServiceMeta(tmp, target, method);
			}else if(annotation instanceof Head) {
				var tmp = (Head)annotation;
				return new ServiceMeta(tmp, target, method);
			}
		}
		return null; // :( The method is not a web service.
	}
}