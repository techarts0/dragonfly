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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.xkit.util.Hotpot;

/**
 * @author rocwon@gmail.com
 */

@WebServlet("/ws/*")
public class ServiceRouter extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public static final int ALLOWED = 0; //OK
	public static final int NO_SUCH_API = -10086;
	public static final int INVALID_SESSION = -10000;

	public int authenticate(HttpServletRequest req, HttpServletResponse response, ServiceMeta service){
		if(service == null) return NO_SUCH_API;
		var context = req.getServletContext();
		var sessionConfig = this.getSessionConfig(context);
		if(!sessionConfig.check()) return ALLOWED;
		if(!service.isPermissionRequired()) return ALLOWED;
		String session = getSession(req), ip = getRemorteAddress(req);
		if(session == null || session.isBlank()) return INVALID_SESSION;
		var uid = req.getParameter(sessionConfig.getUidProperty());
		return this.validate(context, uid, ip,  session);
	}
	
	private SessionConfig getSessionConfig(ServletContext ctx) {
		return (SessionConfig)ctx.getAttribute(SessionConfig.CACHE_KEY);
	}
	
	public static String getRemorteAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(result == null) result = request.getHeader("X-Real-IP");
		return result == null ? request.getRemoteAddr() : result;
	} 
	
	public int validate(ServletContext context, String user, String ip, String session) {
		var config = getSessionConfig(context);
		var result = config.verify(ip, user, session);
		return result ? ALLOWED : INVALID_SESSION;
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
        response.setHeader("Access-Control-Allow-Methods", "POST, GET");
        response.setHeader("Access-Control-Max-Age", "315360000"); //10 years
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
    }
	
	private ServiceMeta getService(ServletContext context, String uri, String method) {
		if(Hotpot.isNull(uri)) return null;
		var rootWebLocator = context.getAttribute(WebService.CACHE_KEY);
		if(Objects.isNull(rootWebLocator)) return null;
		return ((WebLocator)rootWebLocator).matches(uri, method);
	}
		
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		setCharsetEncoding(request, response);
		this.allowsCrossDomainAccess(response);
		var api = request.getPathInfo();
		
		if(api == null) {//Ping Request doesn't take any data
			ping(response, false);
			return; //The PING request has not furthermore actions
		}
		
		var method = request.getMethod(); //HTTP METHOD
		var context = request.getServletContext();
		var service = getService(context, api, method);
		
		int result = authenticate(request, response, service);
						
		if(result == ALLOWED) {
			service.call(request, response);
		}else if(result == NO_SUCH_API) {
			handleUndefinedRequest(api, request, response);
		}else{
			var code = INVALID_SESSION;
			var msg = "Session is invalid.";
			WebContext.respondMessage(response, code, msg);
		}
	}
	
	/**
	 * You can put the session in request header with a customized name "x:session".
	 */
	private String getSession(HttpServletRequest request) {
		var result = request.getHeader("x-session");
		if(result == null) {
			result = request.getParameter("session");
		}
		return result;
	}
	
	private void ping(HttpServletResponse response, boolean async) {
		try {
			response.getWriter().write("OK");
			response.getWriter().flush();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void handleUndefinedRequest(String api, HttpServletRequest request, HttpServletResponse response) {
		WebContext.respondMessage(response, NO_SUCH_API, "No such API");
	}
}