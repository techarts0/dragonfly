package cn.techarts.xkit.web;

public class ServiceConfig {
	/**Session Check*/
	public static boolean SC;
	private String sessionKey;
	private String sessionSalt;
	private int sessionDuration;
	private boolean sessionCheck;
	private String exporterPackage;
		
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
	public String getExporterPackage() {
		return exporterPackage;
	}
	public void setExporterPackage(String exporterPackage) {
		this.exporterPackage = exporterPackage;
	}
}