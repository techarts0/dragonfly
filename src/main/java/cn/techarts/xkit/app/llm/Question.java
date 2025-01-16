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

import java.util.ArrayList;
import java.util.List;
import cn.techarts.xkit.util.Codec;

/**
 * @author rocwon@gmail.com
 */
public class Question {
	private String model;
	private List<Message> messages;
	
	public Question() {}
	
	public Question(String content) {
		this.addMessage(content);
	}
	
	public Question(String role, String content) {
		this.addMessage(role, content);
	}
	
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	public void addMessage(String role, String content) {
		if(messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(new Message(role, content));
	}
	public void addMessage(String content) {
		if(messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(new Message(content));
	}
	public String to(){
		return Codec.toJson(this);
	}
}