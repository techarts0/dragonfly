package cn.techarts.xkit.ioc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

public class Context {
	private Map<String, Craft> crafts;
	public static final String NAME = "context.dragonfly.techarts.cn";
	
	public static Context make(String base, String json, String config) {
		return make(new String[] {base}, new String[] {json}, config);
	}
	
	public static Context make(String[] bases, String[] jsons, String config) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
		var configs = resolveConfiguration(config);
		inventory.setConfigs(configs);
		inventory.start(bases, jsons);
		return new Context(container); 
	}
	
	/**For test purpose only*/
	public static Context make(String base, String json, Map<String, String> config) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
		inventory.setConfigs(config);
		inventory.start(base, json);
		return new Context(container); 
	}
	
	public static Context from(ServletContext context) {
		var obj = context.getAttribute(NAME);
		if(obj == null) return null;
		if(!(obj instanceof Context)) return null;
		return (Context)obj;
	}
	
	private static Map<String, String> resolveConfiguration(String file) {
		var config = new Properties();
		var result = new HashMap<String, String>(64);
		try(var in = new FileInputStream(file)) {
			config.load(in);
			for(var key : config.stringPropertyNames()) {
				result.put(key, config.getProperty(key));
			}
			return result;
		}catch(IOException e) {
			throw new Panic("Failed to load config [" + file + "]", e);
		}
	}
	
	Context(Map<String, Craft> container){
		this.crafts = container;
	}
	
	/**
	 * Get the managed object from the context.
	 */
	public <T> T get(String name, Class<T> t) {
		if(name == null) {
			throw Panic.nullName();
		}
		var bean = crafts.get(name);
		if(bean == null) {
			throw Panic.classNotFound(name);
		}
		return t.cast(bean.getInstance());
	}
	
	/**
	 * Get the managed object from the context.
	 */
	public Object get(String name) {
		if(name == null) {
			throw Panic.nullName();
		}
		var craft = crafts.get(name);
		if(craft == null) {
			throw Panic.classNotFound(name);
		}
		return craft.getInstance();
	}
	
	/**
	 * Cache the IOC context into  SERVLET context, 
	 * call the method {@link from} then to retrieve it.
	 */
	public void cache(ServletContext context) {
		context.setAttribute(NAME, this);
	}
}