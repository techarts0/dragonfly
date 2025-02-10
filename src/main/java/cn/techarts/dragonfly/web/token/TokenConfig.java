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

package cn.techarts.dragonfly.web.token;

/**
 * @author rocwon@gmail.com
 */
public class TokenConfig{
	
	public static final String CACHE_KEY = "config.token.dragonfly";
	
	private String uidProperty;
	
	private String key;
	
	private String salt;
	
	private int duration;
	
	private boolean mandatory;
	
	private Tokenizer tokenizer;
		
	public boolean mandatory() {
		return this.mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}	
	public String getSalt() {
		if(salt != null) return salt;
		return "LikeaBridg3overtR0ub1eDwaTer"; //Default
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getUidProperty() {
		return uidProperty;
	}
	public void setUidProperty(String uidProperty) {
		this.uidProperty = uidProperty;
	}
	public Tokenizer getTokenizer() {
		return tokenizer;
	}
	public void setTokenizer(String tokenizerClass) {
		try {
			var clz = Class.forName(tokenizerClass);
			var obj = clz.getConstructor().newInstance();
			this.tokenizer = (Tokenizer)obj;
		}catch(Exception e) {
			this.tokenizer = new DefaultTokenizer();
		}
	}
}