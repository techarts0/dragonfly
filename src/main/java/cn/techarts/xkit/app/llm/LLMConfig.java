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
package cn.techarts.xkit.app.llm;

import cn.techarts.whale.Valued;

/**
 * @author rocwon@gmail.com
 */
public class LLMConfig {
	@Valued(key="llm.url")
	private String url;
	
	@Valued(key="llm.provider")
	private String provider;
	
	@Valued(key="llm.model")
	private String model;
	
	@Valued(key="llm.appid")
	private String appid;
	
	@Valued(key="llm.appkey")
	private String appkey;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		if(provider == null) return;
		this.provider = provider.toUpperCase();
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}	
}