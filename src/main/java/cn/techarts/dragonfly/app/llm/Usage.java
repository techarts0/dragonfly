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

/**
 * @author rocwon@gmail.com
 */
public class Usage {
	private int prompt_tokens;
	private int completion_tokens;
	private int total_tokens;
	
	public int getPrompt_tokens() {
		return prompt_tokens;
	}
	public void setPrompt_tokens(int prompt_tokens) {
		this.prompt_tokens = prompt_tokens;
	}
	public int getCompletion_tokens() {
		return completion_tokens;
	}
	public void setCompletion_tokens(int completion_tokens) {
		this.completion_tokens = completion_tokens;
	}
	public int getTotal_tokens() {
		return total_tokens;
	}
	public void setTotal_tokens(int total_tokens) {
		this.total_tokens = total_tokens;
	}
}
