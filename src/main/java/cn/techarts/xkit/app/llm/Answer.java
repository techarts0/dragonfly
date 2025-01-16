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

import java.util.Date;
import java.util.List;

/**
 * @author rocwon@gmail.com
 */
public class Answer {
	private String id;
	private String object;
	private String model;
	private Date created;
	private Usage usage;
	private List<Choice> choices;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public Usage getUsage() {
		return usage;
	}
	public void setUsage(Usage usage) {
		this.usage = usage;
	}
	public List<Choice> getChoices() {
		return choices;
	}
	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}
	public String get(int id) {
		if(choices == null) return null;
		if(choices.isEmpty()) return null;
		if(id >= choices.size()) return null;
		var choice = choices.get(id);
		if(choice == null) return null;
		var msg = choice.getMessage();
		if(msg == null) {
			return choice.getFinish_reason();
		}
		return msg.getContent();
	}
	
	public String first() {
		return get(0);
	}
}