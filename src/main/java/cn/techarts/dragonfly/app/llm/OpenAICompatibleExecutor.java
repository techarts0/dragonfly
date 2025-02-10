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
package cn.techarts.dragonfly.app.llm;

import java.util.HashMap;

import cn.techarts.dragonfly.app.helper.Requester;
import cn.techarts.dragonfly.util.Codec;

/**
 * @author rocwon@gmail.com
 */
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