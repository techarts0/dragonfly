package cn.techarts.xkit.web;

import java.io.Serializable;

import cn.techarts.xkit.util.Converter;
import cn.techarts.xkit.util.Hotchpotch;

public class UserSession implements Serializable {
	private static final long serialVersionUID = 1L;
	public static int DURATION = 0;
	public static String KEY = null;
	public static String SALT = null;
	
	public static void init(String salt, int duration, String key) {
		KEY = key;
		SALT = salt;
		DURATION = duration;
	}
	
	public static boolean verify(String ip, int userId, String session) {
		var tmp = Hotchpotch.decrypt(session, Hotchpotch.toBytes(KEY));
		if(tmp == null) return false; //An invalid session
		var bgn = Converter.toInt(tmp.substring(0, 8));
		if(minutes() - bgn > DURATION) return false;
		var result = (ip != null ? ip : "0000") + userId + SALT;
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public static String generate(String ip, int userId) {
		var result = (ip != null ? ip : "0000") + userId;
		var minutes = String.valueOf(minutes());
		result = minutes.concat(result).concat(SALT);
		return Hotchpotch.encrypt(result, Hotchpotch.toBytes(KEY));
	}
	
	public static int minutes() {
		return (int)(System.currentTimeMillis() / 60000);
	}
}