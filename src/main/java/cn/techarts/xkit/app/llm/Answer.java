package cn.techarts.xkit.app.llm;

import java.util.Date;
import java.util.List;

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