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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import cn.techarts.dragonfly.web.token.ClientContext;
import cn.techarts.dragonfly.web.token.TokenConfig;

/**
 * <p>javax & jakarta</p>
 * @author rocwon@gmail.com
 */
public class ServiceRouter extends HttpServlet{
	public static boolean JSONRPC = false;
	private static final long serialVersionUID = 1L;
	public static final int ALLOWED = 0; //OK
	public static final int NO_SUCH_API = -10086;
	public static final int INVALID_TOKEN = -10000;
	private static final String METHODS = "GET, POST, PUT, DELETE, HEAD, PATCH";
	
	public int authenticate(HttpServletRequest req, HttpServletResponse response, ServiceMeta service){
		if(service == null) return NO_SUCH_API;
		var context = req.getServletContext();
		var tokenConfig = getTokenConfig(context);
		//Global Settings
		if(!tokenConfig.mandatory()) return ALLOWED;
		//API Settings
		if(!service.isMandatory()) return ALLOWED;
		var token = getToken(req);
		if(Objects.isNull(token)) return INVALID_TOKEN;
		var ip = getRemorteAddress(req);
		var uid = req.getParameter(tokenConfig.getUidProperty());
		return validate(context, uid, ip,  token, getUserAgent(req));
	}
	
	private TokenConfig getTokenConfig(ServletContext ctx) {
		var tmp = ctx.getAttribute(TokenConfig.CACHE_KEY);
		return tmp == null ? new TokenConfig() : (TokenConfig)tmp;
	}
	
	public static String getRemorteAddress(HttpServletRequest request) {
		var result = request.getHeader("X-Real-IP");
		if(result == null) {
			result = request.getHeader("x-forwarded-for");;
		}
		return result != null ? result : request.getRemoteAddr();
	} 
	
	private String getUserAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}
	
	public int validate(ServletContext context, String user, String ip, String token, String ua) {
		var config = getTokenConfig(context);
		var client = new ClientContext(ip, ua, user);
		var result = config.getTokenizer().verify(client,config, token);
		return result ? ALLOWED : INVALID_TOKEN;
	}
	
	private void setCharsetEncoding(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
		}catch(UnsupportedEncodingException e) {
			//You are a son of bitch if UTF-8 is not supported.
		}
	}
	
	public void allowsCrossDomainAccess(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", METHODS);
        response.setHeader("Access-Control-Max-Age", "315360000"); //10 years
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
    }
	
	private ServiceMeta getService(ServletContext context, String uri, String method) {
		if(uri == null || uri.isEmpty()) return null;
		var rootWebLocator = context.getAttribute(WebLocator.CACHE_KEY);
		if(Objects.isNull(rootWebLocator)) return null;
		return ((WebLocator)rootWebLocator).matches(uri, method);
	}
		
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		setCharsetEncoding(request, response);
		this.allowsCrossDomainAccess(response);
		var api = request.getPathInfo();
		var method = request.getMethod(); //HTTP METHOD
		var context = request.getServletContext();
		var service = getService(context, api, method);
		
		int result = authenticate(request, response, service);
						
		if(result == ALLOWED) {
			service.call(request, response, JSONRPC);
		}else if(result == NO_SUCH_API) {
			handleUndefinedRequest(api, request, response);
		}else{
			var code = INVALID_TOKEN;
			var msg = "Token is invalid.";
			WebContext.respondMessage(response, code, msg);
		}
	}
	
	/**
	 * It's compatible to JWT
	 */
	private String getToken(HttpServletRequest request) {
		var result = request.getHeader("token");
		if(result != null) return result;
		result = request.getHeader("Authorization");
		if(result == null || result.length() < 8) return null;
		var bearer = result.startsWith("Bearer ");
		return bearer ? result.substring(7) : null;
	}
	
	protected void handleUndefinedRequest(String api, HttpServletRequest request, HttpServletResponse response) {
		WebContext.respondMessage(response, NO_SUCH_API, "No such API");
	}
}