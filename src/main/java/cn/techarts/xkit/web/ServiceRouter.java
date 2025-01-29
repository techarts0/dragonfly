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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.techarts.xkit.app.helper.Empty;
import cn.techarts.xkit.web.token.TokenConfig;

/**
 * @author rocwon@gmail.com
 */
public class ServiceRouter extends HttpServlet{
	private static final long serialVersionUID = 1L;
	public static final int ALLOWED = 0; //OK
	public static final int NO_SUCH_API = -10086;
	public static final int INVALID_SESSION = -10000;
	public static final String METHODS = "GET, POST, PUT, DELETE, HEAD, PATCH";
	
	public int authenticate(HttpServletRequest req, HttpServletResponse response, ServiceMeta service){
		if(service == null) return NO_SUCH_API;
		var context = req.getServletContext();
		var sessionConfig = getTokenConfig(context);
		if(!sessionConfig.required()) return ALLOWED;
		if(!service.isPermissionRequired()) return ALLOWED;
		String token = getToken(req), ip = getRemorteAddress(req);
		if(token == null || token.isBlank()) return INVALID_SESSION;
		var uid = req.getParameter(sessionConfig.getUidProperty());
		return validate(context, uid, ip,  token, getUserAgent(req));
	}
	
	private TokenConfig getTokenConfig(ServletContext ctx) {
		var tmp = ctx.getAttribute(TokenConfig.CACHE_KEY);
		return tmp == null ? new TokenConfig() : (TokenConfig)tmp;
	}
	
	public static String getRemorteAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(result == null) result = request.getHeader("X-Real-IP");
		return result == null ? request.getRemoteAddr() : result;
	} 
	
	private String getUserAgent(HttpServletRequest request) {
		return request.getHeader("user-agent");
	}
	public int validate(ServletContext context, String user, String ip, String token, String ua) {
		var config = getTokenConfig(context);
		var result = config.getTokenizer()
							.verify(ip, user, ua, 
							config.getTokenSalt(), 
							config.getTokenDuration(), 
							config.getTokenKey(), 
							token);
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
        response.setHeader("Access-Control-Allow-Methods", METHODS);
        response.setHeader("Access-Control-Max-Age", "315360000"); //10 years
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
    }
	
	private ServiceMeta getService(ServletContext context, String uri, String method) {
		if(Empty.is(uri)) return null;
		var rootWebLocator = context.getAttribute(WebLocator.CACHE_KEY);
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
			var msg = "Token is invalid.";
			WebContext.respondMessage(response, code, msg);
		}
	}
	
	/**
	 * You can put the token in request header with the name "token" or "x-token".
	 */
	private String getToken(HttpServletRequest request) {
		var result = request.getHeader("token");
		if(result != null) return result;
		result = request.getHeader("Authorization");
		if(result == null || result.length() < 8) return null;
		var bearer = result.startsWith("Bearer ");
		return bearer ? result.substring(7) : null;
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