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
import cn.techarts.xkit.web.restful.Patch;
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
	private MediaType mediaType = MediaType.JSON;
	
	private List<String> arguments = null;
	
	public ServiceMeta(Get m, Object obj, Method method, String prefix) {
		this.httpMethod= "GET";
		var uri = prefix.concat(m.value());
		setAttrs(obj, method, uri, m.permission(), m.media());
	}
	
	public ServiceMeta(Post m, Object obj, Method method, String prefix) {
		this.httpMethod= "POST";
		var uri = prefix.concat(m.value());
		setAttrs(obj, method, uri, m.permission(), m.media());
	}
	
	public ServiceMeta(Put m, Object obj, Method method, String prefix) {
		this.httpMethod= "PUT";
		var uri = prefix.concat(m.value());
		setAttrs(obj, method, uri, m.permission(), m.media());
	}
	
	public ServiceMeta(Patch m, Object obj, Method method, String prefix) {
		this.httpMethod= "PATCH";
		var uri = prefix.concat(m.value());
		setAttrs(obj, method, uri, m.permission(), m.media());
	}
	
	public ServiceMeta(Delete m, Object obj, Method method, String prefix) {
		this.httpMethod= "DELETE";
		var uri = prefix.concat(m.value());
		setAttrs(obj, method, uri, m.permission(), m.media());
	}
	
	public ServiceMeta(Head m, Object obj, Method method, String prefix) {
		this.httpMethod= "HEAD";
		var uri = prefix.concat(m.value());
		setAttrs(obj, method, uri, m.permission(), m.media());
	}
	
	public ServiceMeta(WebMethod m, Object obj, Method method, String prefix) {
		this.httpMethod= m.method().toUpperCase();
		var uri = prefix.concat(m.uri());
		setAttrs(obj, method, uri, m.permission(), m.media());
		this.restful = m.restful(); //Recover the default value.
	}	
	
	private void setAttrs(Object object, Method method, String uri, boolean permission, MediaType type) {
		this.uri = uri;
		this.restful = true;
		this.object = object;
		this.method = method;
		this.mediaType = type;
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
			p.respondAsJson(method.invoke(object, p), mediaType);
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
	
	public static ServiceMeta to(Method method, Object target, String prefix) {
		var result = method.getDeclaredAnnotations();
		if(result == null || result.length == 0) return null;
		for(var annotation : result) {
			if(annotation instanceof WebMethod) {
				return new ServiceMeta((WebMethod)annotation, target, method, prefix);
			}else if(annotation instanceof Get) {
				return new ServiceMeta((Get)annotation, target, method, prefix);
			}else if(annotation instanceof Post) {
				return new ServiceMeta((Post)annotation, target, method, prefix);
			}else if(annotation instanceof Put) {
				return new ServiceMeta((Put)annotation, target, method, prefix);
			}else if(annotation instanceof Delete) {
				return new ServiceMeta((Delete)annotation, target, method, prefix);
			}else if(annotation instanceof Head) {
				return new ServiceMeta((Head)annotation, target, method, prefix);
			}else if(annotation instanceof Patch) {
				return new ServiceMeta((Patch)annotation, target, method, prefix);
			}
		}
		return null; // :( The method is not a web service or unsupported HTTP method.
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}
}