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
public class TokenConfig{
	
	public static final String CACHE_KEY = "config.token.techarts";
	
	private String uidProperty;
	
	private String tokenKey;
	
	private String tokenSalt;
	
	private int tokenDuration;
	
	private boolean tokenRequired;
	
	private Tokenizer tokenizer;
		
	public boolean required() {
		return tokenRequired;
	}
	public void setTokenRequired(boolean required) {
		this.tokenRequired = required;
	}	
	public String getTokenSalt() {
		if(tokenSalt != null) return tokenSalt;
		return "LikeaBridg3overtR0ub1eDwaTer"; //Default
	}
	public void setTokenSalt(String tokenSalt) {
		this.tokenSalt = tokenSalt;
	}
	public int getTokenDuration() {
		return tokenDuration;
	}
	public void setTokenDuration(int tokenDuration) {
		this.tokenDuration = tokenDuration;
	}
	public String getTokenKey() {
		return tokenKey;
	}
	public void setTokenKey(String tokenKey) {
		this.tokenKey = tokenKey;
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