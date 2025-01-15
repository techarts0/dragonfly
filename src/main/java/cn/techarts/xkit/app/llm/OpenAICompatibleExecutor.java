package cn.techarts.xkit.app.llm;

import java.util.HashMap;
import cn.techarts.xkit.util.Codec;
import cn.techarts.xkit.app.helper.Requester;

public class OpenAICompatibleExecutor implements GptHelper{
	
	private LLMConfig config;
	
	public OpenAICompatibleExecutor(LLMConfig config) {
		this.config = config;
	}
	
	@Override
	public Answer ask(Question question) {
		var header = new HashMap<String, String>();
		//header.put("Content-Type", Requester.CONTENT_TYPE_JSON);
		header.put("Authorization", "Bearer " + config.getAppkey());
		var result = Requester.post(config.getUrl(), question.to(), header);
		return result == null ? null : Codec.decodeJson(result, Answer.class);
	}

	public LLMConfig getConfig() {
		return config;
	}

	public void setConfig(LLMConfig config) {
		this.config = config;
	}
}