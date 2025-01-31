package cn.techarts.xkit.web.token;

public class ClientContext {
	private String ip;
	private String ua;
	private String user;
	
	/**
	 * @param ip ip address
	 * @param ua User-Agent
	 * @param user (id, email, mobile, username...)
	 */
	public ClientContext(String ip, String ua, String user) {
		this.ip = ip;
		this.ua = ua;
		this.user = user;
	}
	
	/**
	 * IP Address
	 **/
	public String getIp() {
		return ip;
	}
	
	/**
	 * IP Address
	 **/
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	/**
	 * User-Agent
	 **/
	public String getUa() {
		return ua;
	}
	
	/**
	 * User-Agent
	 **/
	public void setUa(String ua) {
		this.ua = ua;
	}
	
	/**
	 * User(id, email, mobile, username...)
	 **/
	public String getUser() {
		return user;
	}
	
	/**
	 * User(id, email, mobile, username...)
	 **/
	public void setUser(String user) {
		this.user = user;
	}
}