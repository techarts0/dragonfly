package cn.techarts.xkit.web;

import javax.inject.Inject;

import cn.techarts.xkit.ioc.Valued;

/**
 * Web APP configuration
 */
public class AppConfig {
	/**Session Check*/
	public static boolean SC;
	
	@Inject
	@Valued(key="session.key")
	private String sessionKey;
	
	@Inject
	@Valued(key="session.salt")
	private String sessionSalt;
	
	@Inject
	@Valued(key="session.duration")
	private int sessionDuration;
	
	@Inject
	@Valued(key="session.check")
	private boolean sessionCheck;
	
	@Inject
	@Valued(key="web.service.package")
	private String servicePackage;
		
	public boolean isSessionCheck() {
		return sessionCheck;
	}
	public void setSessionCheck(boolean check) {
		SC = check; //An alias
		this.sessionCheck = check;
	}	
	public String getSessionSalt() {
		if(sessionSalt != null) return sessionSalt;
		return "LikeaBridg3overtR0ub1eDwaTer"; //Default
	}
	public void setSessionSalt(String sessionSalt) {
		this.sessionSalt = sessionSalt;
	}
	public int getSessionDuration() {
		return sessionDuration;
	}
	public void setSessionDuration(int sessionDuration) {
		this.sessionDuration = sessionDuration;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}	
	public String getServicePackage() {
		return servicePackage;
	}
	public void setServicePackage(String webServicePackage) {
		this.servicePackage = webServicePackage;
	}
}