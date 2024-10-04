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
	
	public ServiceMeta(Get get, Object object, Method method) {
		this.uri = get.uri();
		this.object = object;
		this.method = method;
		this.setRestful(true);
		this.httpMethod= "GET";
		this.permission = get.permission();
	}
	
	public ServiceMeta(Post get, Object object, Method method) {
		this.uri = get.uri();
		this.object = object;
		this.method = method;
		this.setRestful(true);
		this.httpMethod= "POST";
		this.permission = get.permission();
	}
	
	public ServiceMeta(Put get, Object object, Method method) {
		this.uri = get.uri();
		this.object = object;
		this.method = method;
		this.setRestful(true);
		this.httpMethod= "PUT";
		this.permission = get.permission();
	}
	
	public ServiceMeta(Delete get, Object object, Method method) {
		this.uri = get.uri();
		this.object = object;
		this.method = method;
		this.setRestful(true);
		this.httpMethod= "DELETE";
		this.permission = get.permission();
	}
	
	public ServiceMeta(WebMethod m, Object object, Method method) {
		this.uri = m.uri();
		this.object = object;
		this.method = method;
		this.setRestful(m.restful());
		this.permission = m.permission();
		this.httpMethod= m.method().toUpperCase();
	}
	
	/**
	 * Get the last part of the whole URI
	 */
	public static String extractName(String uri) {
		if(uri == null) return null;
		if(uri.indexOf('/') <= 0) return uri;
		var pathes = uri.split("/");
		return pathes[pathes.length - 1];
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
			e.printStackTrace();
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
		var wm = method.getAnnotation(WebMethod.class);
		if(wm != null && wm.uri() != null) {
			return new ServiceMeta(wm, target, method);
		}
		var gr = method.getAnnotation(Get.class);
		if(gr != null && gr.uri() != null) {
			return new ServiceMeta(gr, target, method);
		}
		var pr = method.getAnnotation(Post.class);
		if(pr != null && pr.uri() != null) {
			return new ServiceMeta(pr, target, method);
		}
		var rp = method.getAnnotation(Put.class);
		if(rp != null && rp.uri() != null) {
			return new ServiceMeta(rp, target, method);
		}		
		var dr = method.getAnnotation(Delete.class);
		if(dr == null || dr.uri() == null) return null;
		return new ServiceMeta(dr, target, method);
	}
}