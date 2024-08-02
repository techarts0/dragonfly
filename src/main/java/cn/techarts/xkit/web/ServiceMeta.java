package cn.techarts.xkit.web;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Spliter;

public final class ServiceMeta {
	private Object object;
	private Method method;
	private String uri = null;
	private String httpMethod;
	private boolean sessionRequired = true;
	
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
		if(Empty.is(uri)) return null;
		if(uri.indexOf('/') <= 0) return uri;
		var paths = Spliter.split(uri, '/');
		return paths.get(paths.size() - 1);
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

	public boolean isSessionRequired() {
		return sessionRequired;
	}

	public void setSessionRequired(boolean authorizationRequired) {
		this.sessionRequired = authorizationRequired;
	}
}
