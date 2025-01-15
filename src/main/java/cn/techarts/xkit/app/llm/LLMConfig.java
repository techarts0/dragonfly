package cn.techarts.xkit.app.llm;

import cn.techarts.whale.Valued;

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