package cn.techarts.xkit.app.llm;

import java.util.ArrayList;
import java.util.List;
import cn.techarts.xkit.util.Codec;

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