package cn.techarts.xkit.ioc;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import org.apache.logging.log4j.Logger;
import cn.techarts.xkit.util.Hotchpotch;

public class Context implements AutoCloseable{
	private Map<String, Craft> crafts;
	public static final String NAME = "context.dragonfly.techarts.cn";
	private static final Logger LOGGER = Hotchpotch.getLogger(Context.class);
	
	public static Context make(String base, String json, String config) {
		return make(new String[] {base}, new String[] {json}, config);
	}
	
	public void close() {
		if(crafts == null) return;
		if(crafts.isEmpty()) return;
		for(var craft : crafts.values()) {
			var obj = craft.getInstance();
			if(obj == null) continue;
			if(obj instanceof AutoCloseable) {
				try {
					((AutoCloseable)obj).close();
				}catch(Exception e) {
					LOGGER.error("Failed to close: " + craft.getName(), e);
				}
			}
		}
	}
	
	public static Context make(String[] bases, String[] jsons, String config) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
		var configs = Hotchpotch.resolveConfiguration(config);
		inventory.setConfigs(configs);
		inventory.start(bases, jsons);
		return new Context(container); 
	}
	
	public static Context make(String[] bases, String[] jsons, Map<String, String> configs) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
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
	
	Context(Map<String, Craft> container){
		this.crafts = container;
		LOGGER.info("Initialized the IOC container.");
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