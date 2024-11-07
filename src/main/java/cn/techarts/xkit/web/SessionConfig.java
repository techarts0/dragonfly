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

import cn.techarts.xkit.util.Converter;
import cn.techarts.xkit.util.Hotpot;

/**
 * @author rocwon@gmail.com
 */
public class SessionConfig {
	
	public static final String CACHE_KEY = "config.session.techarts";
	
	private String uidProperty;
	
	private String sessionKey;
	
	private String sessionSalt;
	
	private int sessionDuration;
	
	private boolean sessionCheck;
		
	public boolean check() {
		return sessionCheck;
	}
	public void setSessionCheck(boolean check) {
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
	public String getUidProperty() {
		return uidProperty;
	}
	public void setUidProperty(String uidProperty) {
		this.uidProperty = uidProperty;
	}
	
	//////////////////////////////////////////////////
	
	public boolean verify(String ip, String userId, String session) {
		var tmp = Hotpot.decrypt(session, Hotpot.toBytes(sessionKey));
		if(tmp == null) return false; //An invalid session
		var bgn = Converter.toInt(tmp.substring(0, 8));
		if(minutes() - bgn > sessionDuration) return false;
		var result = (ip != null ? ip : "0000") + userId + sessionSalt;
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public String generate(String ip, String userId) {
		var result = (ip != null ? ip : "0000") + userId;
		var minutes = String.valueOf(minutes());
		result = minutes.concat(result).concat(sessionSalt);
		return Hotpot.encrypt(result, Hotpot.toBytes(sessionKey));
	}
	
	public static int minutes() {
		return (int)(System.currentTimeMillis() / 60000);
	}
}