package cn.techarts.xkit.ioc;

import java.util.List;
import java.util.Map;

/**
 * An managed object definition in JSON file(crafts.json).
 */
public class Node {
	private String name;
	private String type;
	private boolean singleton;
	/**
	 * Inject arguments into constructor<br>
	 * "args": ["REF:User:com.xx.yy.User", "KEY:jdbc.driver:com.mysql.jdbc.Driver", 3306]
	 */
	private List<Object> args;
	/**
	 * Inject values into properties
	 * "props":{"name":"KEY:user.name", "age":25, "contactor": "REF:User"}
	 */
	private Map<String, Object> props;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isSingleton() {
		return singleton;
	}
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	public List<Object> getArgs() {
		return args;
	}
	public void setArgs(List<Object> args) {
		this.args = args;
	}
	public Map<String, Object> getProps() {
		return props;
	}
	public void setProps(Map<String, Object> props) {
		this.props = props;
	}	
	public boolean isPropInject() {
		return (props != null && !props.isEmpty());
	}
	
	public void resetName(String name) {
		if(this.name != null) return;
		if(!this.name.isBlank()) return;
		this.name = name; //Class short name
	}
}