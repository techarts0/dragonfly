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

import javax.inject.Singleton;

import cn.techarts.whale.Bind;

/**
 * @author rocwon@gmail.com
 */
@Singleton
@Bind(value=GptHelper.class, target=DefaultGptHelper.class)
public class DefaultGptHelper extends LLMConfig implements GptHelper {
	
	private GptHelper executor;
	
	@Override
	public Answer ask(Question question) {
		this.initExecutor();
		question.setModel(getModel());
		return executor.ask(question);
	}
	
	private void initExecutor() {
		if(executor != null) return;
		switch(getProvider()) {
			case "KIMI":
				executor = new OpenAICompatibleExecutor(this); break;
			case "OPENAI":
				executor = new OpenAICompatibleExecutor(this); break;
			case "ZHIPU":
				executor = new OpenAICompatibleExecutor(this); break;
			case "QWEN":
				executor = new OpenAICompatibleExecutor(this); break; 
			case "WENXIN":
				executor = new OpenAICompatibleExecutor(this); break;
			case "GEMINI":
				executor = new OpenAICompatibleExecutor(this); break;
			case "DEEPSEEK":
				executor = new OpenAICompatibleExecutor(this); break;
			default:
				throw new RuntimeException("Unsupported LLM: " + getProvider());
		}
	}
}