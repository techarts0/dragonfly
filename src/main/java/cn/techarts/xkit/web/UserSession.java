package cn.techarts.xkit.web;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import cn.techarts.jhelper.Cryptor;
import cn.techarts.jhelper.Empty;
import cn.techarts.jhelper.Time;

import java.util.HashMap;

public class UserSession implements Serializable {
	private static final long serialVersionUID = 1L;
	public static int DURATION = 0;
	public static String KEY = null;
	public static String SALT = null;
	
	private Map<String, String> sessions;
	
	public static void init(String salt, int duration, String key) {
		KEY = key;
		SALT = salt;
		DURATION = duration;
	}
	
	public UserSession(){
		this.sessions = new HashMap<>(4);
	}
	
	public UserSession(int agent, String session){
		sessions = new HashMap<>(4);
		this.appendNewDevice(session, agent);
	}
	
	public UserSession(int agent, String ip, String session){
		sessions = new HashMap<>(4);
		this.appendNewDevice(ip, session, agent);
	}
	
	public void appendNewDevice(String session, int agent) {
		var sessionInfo = new StringBuilder(session)
		   .append(',').append(new Date().getTime());
		if(sessions == null) sessions =  new HashMap<>(4);
		String key = String.valueOf(agent);
		if(sessions.containsKey(key)) sessions.remove(key);
		this.sessions.put(key, sessionInfo.toString());
	}
	
	public void appendNewDevice(String ip, String session, int agent) {
		var sessionInfo = new StringBuilder(session)
							 .append(',').append(ip);
		if(sessions == null) sessions =  new HashMap<>(4);
		String key = String.valueOf(agent);
		if(sessions.containsKey(key)) sessions.remove(key);
		this.sessions.put(key, sessionInfo.toString());
	}
	
	public static boolean verify(String ip, int userId, String session) {
		var tmp = Cryptor.decrypt(session, Cryptor.toBytes(KEY));
		if(Empty.is(tmp)) return false; //An invalid session
		var bgn = Integer.parseInt(tmp.substring(0, 8));
		if(Time.minutes() - bgn > DURATION) return false;
		var result = (ip != null ? ip : "0000") + userId + SALT;
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public void setSessions(Map<String, String> sessions) {
		this.sessions = sessions;
	}
	
	public Map<String, String> getSessions(){
		if(this.sessions == null) {
			this.sessions =  new HashMap<>(4);
		}
		return this.sessions;
	}
	
	private static String generateWithKey(String ip, int ua, int userId) {
		var result = (ip != null ? ip : "0000") + ua + userId;
		var minutes = String.valueOf(Time.minutes());
		result = minutes.concat(result).concat(SALT);
		return Cryptor.encrypt(result, Cryptor.toBytes(KEY));
	}
	
	public static String generate(String ip, int ua, int userId) {
		return generateWithKey(ip, ua, userId);
	}
}