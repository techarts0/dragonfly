package cn.techarts.xkit.ioc;

import java.util.Map;
import java.util.logging.Logger;
import java.util.HashMap;

import javax.servlet.ServletContext;
import cn.techarts.xkit.util.Hotpot;

public class Context implements AutoCloseable{
	private Map<String, Craft> crafts;
	public static final String NAME = "context.dragonfly.techarts.cn";
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public static Context make(String base, String path, String config) {
		return make(new String[] {base}, new String[] {path}, config);
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
					LOGGER.severe("Failed to close " + craft.getName() + ": " + e.getMessage());
				}
			}
		}
	}
	
	public static Context make(String[] bases, String[] paths, String config) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
		var configs = Hotpot.resolveProperties(config);
		inventory.setConfigs(configs);
		inventory.start(bases, paths);
		return new Context(container); 
	}
	
	public static Context make(String[] bases, String[] paths, Map<String, String> configs) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
		inventory.setConfigs(configs);
		inventory.start(bases, paths);
		return new Context(container); 
	}
	
	/**For test purpose only*/
	public static Context make(String base, String path, Map<String, String> config) {
		var container = new HashMap<String, Craft>(512);
		var inventory = new Factory(container);
		inventory.setConfigs(config);
		inventory.start(base, path);
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