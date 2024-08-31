package cn.techarts.xkit.web;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

public class ServiceCache {
	
	private static Map<String, ServiceMeta> webservices = new LinkedHashMap<>(512);
	
	/**
	 * The caller must ensure that the parameters are legal
	 */
	public static void cacheService(String uri, ServiceMeta service) {
		if(uri == null) return;
		if(service == null) return;
		webservices.put(uri, service);
	}
	
	/**
	 * The caller must ensure that the parameters are legal<p>
	 * There are 2 different APIs<p>
	 * <B><I>get</I></B>/user/login<br>
	 * <B><I>post</I></B>/user/login
	 */
	public static void cacheService(String uri, String method, ServiceMeta service) {
		if(uri == null) return;
		if(method == null) return;
		if(service == null) return;
		webservices.put(method.toLowerCase().concat(uri), service);
	}
	
	/**For Example:<p> 
	 * get/login<p>
	 * post/users
	 */
	public static ServiceMeta getService(String uri, String method) {
		if(uri == null || uri.isBlank()) return null;
		if(!ServiceMeta.restful) {
			return webservices != null ? webservices.get(uri) : null; 
		}else {
			var m = method.toLowerCase().concat(uri);
			return webservices != null ? webservices.get(m) : null;
		}
	}
	
	public static Set<String> getServiceNames(){
		return webservices.keySet();
	}
	
	public static void cacheSession(int userId, String ip, int agent, String session){
		if(userId == 0 || session == null) return;
		
	}
	
	public static boolean checkSession(int userId, String ip, String session, int agent){
		if(userId == 0 || session == null) return false;
		return false;
	}
}