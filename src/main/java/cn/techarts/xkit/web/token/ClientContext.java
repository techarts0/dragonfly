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
package cn.techarts.xkit.web.token;

/**
 * @author rocwon@gmail.com
 */
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