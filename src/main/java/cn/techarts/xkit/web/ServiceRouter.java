package cn.techarts.xkit.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ws/*")
public class ServiceRouter extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public static final int ALLOWED = 0; //OK
	public static final int NO_SUCH_API = -10086;
	public static final int INVALID_SESSION = -10000;

	public int authenticate(HttpServletRequest req, HttpServletResponse response, ServiceMeta service){
		if(service == null) return NO_SUCH_API;
		if(!SessionConfig.SC) return ALLOWED;
		if(!service.isPermissionRequired()) return ALLOWED;
		String session = getSession(req), ip = getRemorteAddress(req);
		if(session == null || session.isBlank()) return INVALID_SESSION;
		return checkSession(Integer.parseInt(req.getParameter("uid")), ip,  session);
	}
	
	public static String getRemorteAddress(HttpServletRequest request) {
		var result = request.getHeader("x-forwarded-for");
		if(result == null) result = request.getHeader("X-Real-IP");
		return result == null ? request.getRemoteAddr() : result;
	} 
	
	public int checkSession(int user, String ip, String session) {
		boolean result = UserSession.verify(ip, user, session);
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
		var service = ServiceCache.getService(api, method);
		
		int result = authenticate(request, response, service);
						
		if(result == ALLOWED) {
			service.call(request, response, method);
		}else if(result == NO_SUCH_API) {
			handleUndefinedRequest(api, request, response);
		}else{
			var code = INVALID_SESSION;
			var msg = "Error: an invalid session.";
			WebContext.respondAsJson(response, result, code, msg);
		}
	}
	
	/**
	 * You can put the session in request header with a customized name "x:session".
	 */
	private String getSession(HttpServletRequest request) {
		var result = request.getHeader("x:session");
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
		WebContext.respondAsJson(response, null, NO_SUCH_API, "No such API.");
	}
}