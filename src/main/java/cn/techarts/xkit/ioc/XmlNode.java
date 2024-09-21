package cn.techarts.xkit.ioc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.techarts.xkit.util.Converter;

/**
 * An managed craft definition in JSON file(crafts.json).
 */
public class Element {
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
		if(name == null) return type;
		return name.isBlank() ? type : name;
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
	public void setSingleton(String singleton) {
		this.singleton = Converter.toBoolean(singleton);
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
	public Class<?> instance() throws ClassNotFoundException{
		return Class.forName(this.type);
	}
	public void addArg(String ref, String key, String val, String type) {
		var result = new StringBuilder();
		if(this.args == null) {
			this.args = new ArrayList<Object>();
		}
		if(ref != null && !ref.isEmpty()) {
			result.append("REF:").append(ref); 
		}else if(key != null && !key.isEmpty()) {
			result.append("KEY:").append(key);
		}else if(val != null && !val.isEmpty()) {
			result.append("VAL:").append(val);
		}
		if(type != null && !type.isEmpty()) {
			result.append(":").append(type);
		}
		this.args.add(result.toString());
	}
	
	public void addProp(String ref, String key, String val, String name, String type) {
		if(this.props == null) {
			this.props = new HashMap<String, Object>();
		}
		var result = new StringBuilder();
		
		if(ref != null && !ref.isEmpty()) {
			result.append("REF:").append(ref); 
		}else if(key != null && !key.isEmpty()) {
			result.append("KEY:").append(key);
		}else if(val != null && !val.isEmpty()) {
			result.append("VAL:").append(val);
		}
		if(type != null && !type.isEmpty()) {
			result.append(":").append(type);
		}
		this.props.put(name, result.toString());
	}
}