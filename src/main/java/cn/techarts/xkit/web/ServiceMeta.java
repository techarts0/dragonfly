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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rocwon@gmail.com
 */
public final class ServiceMeta {
	private Object object;
	private Method method;
	private String uri = null;
	private String httpMethod;
	private boolean permission = true;
	
	public static boolean restful = false;
	
	public ServiceMeta(String uri, Object object, Method method, String m) {
		this.uri = uri;
		this.httpMethod= m;
		this.object = object;
		this.method = method;
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
	
	public void call(HttpServletRequest request, HttpServletResponse response, String m) {
		if(method == null || object == null) return;
		if(restful && !httpMethod.equals(m)) return;
		try {
			var p = new WebContext(request, response);
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
}