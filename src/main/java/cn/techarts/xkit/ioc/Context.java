package cn.techarts.xkit.ioc;

import java.util.Map;
import java.util.logging.Logger;
import java.util.HashMap;

import javax.servlet.ServletContext;
import cn.techarts.xkit.util.Hotpot;

public class Context implements AutoCloseable{
	private Map<String, Craft> crafts;
	private Map<String, String> configs;
	public static final String NAME = "context.dragonfly.techarts.cn";
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public static Context make(String base, String path, String config) {
		return make(new String[] {base}, new String[] {path}, config);
	}
	
	@Override
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
	
	/**
	 * The method will scan multiple class-paths and XML files.
	 * @param bases The base class-path of packages.
	 * @param xmlResources The XML beans file paths.
	 * @param config The configuration file path.
	 */
	public static Context make(String[] bases, String[] xmlResources, String config) {
		var container = new HashMap<String, Craft>(512);
		var factory = new Factory(container);
		var configs = Hotpot.resolveProperties(config);
		factory.setConfigs(configs);
		factory.start(bases, xmlResources);
		return new Context(container, configs); 
	}
	
	/**
	 * The method will scan multiple class-paths and XML files.
	 * @param bases The base class-path of packages.
	 * @param xmlResources The XML beans file paths.
	 */
	public static Context make(String[] bases, String[] xmlResources, Map<String, String> configs) {
		var container = new HashMap<String, Craft>(512);
		var factory = new Factory(container);
		factory.setConfigs(configs);
		factory.start(bases, xmlResources);
		return new Context(container, configs); 
	}
		
	/**
	 * @param base The base class-path of package.
	 * @param xmlResource The XML beans file path.
	 */
	public static Context make(String base, String xmlResource, Map<String, String> configs) {
		var container = new HashMap<String, Craft>(512);
		var factory = new Factory(container);
		factory.setConfigs(configs);
		factory.start(base, xmlResource);
		return new Context(container, configs); 
	}
	
	/**
	 * @param base The base class-path of package.
	 * @param xmlResource The XML beans file path.
	 */
	public static Context make(String base, String xmlResource) {
		var container = new HashMap<String, Craft>(512);
		var factory = new Factory(container);
		factory.setConfigs(Map.of());
		factory.start(base, xmlResource);
		return new Context(container, Map.of()); 
	}
	
	/**
	 * @param base The base class-path of package.
	 */
	public static Context make(String base) {
		var container = new HashMap<String, Craft>(512);
		var factory = new Factory(container);
		factory.setConfigs(Map.of());
		factory.start(base, null);
		return new Context(container, Map.of()); 
	}
	
	/**
	 * @param base The base class-path of package.
	 * @param xmlResource The XML beans file path.
	 * @param extras Extra managed object class names you append manually.
	 */
	public static Context make(String base, String xmlResource, Map<String, String> configs, String[] extras) {
		var container = new HashMap<String, Craft>(512);
		var factory = new Factory(container);
		factory.setConfigs(configs);
		factory.start(base, xmlResource, extras);
		return new Context(container, configs); 
	}
	
	/**
	 * Construct an empty context.
	 */
	public static Context make() {
		var container = new HashMap<String, Craft>(128);
		return new Context(container, new HashMap<>());
	}
	
	public static Context make(Map<String, String> configs) {
		return new Context(new HashMap<>(128), configs);
	}
	
	/**
	 * Retrieve the context from SERVLET context.(Web Application)
	 */
	public static Context from(ServletContext context) {
		var obj = context.getAttribute(NAME);
		if(obj == null) return null;
		if(!(obj instanceof Context)) return null;
		return (Context)obj;
	}
	
	Context(Map<String, Craft> container, Map<String, String> configs){
		this.configs = configs == null ? Map.of() : configs;
		this.crafts = container == null ? Map.of() : container;
		LOGGER.info("Initialized the IOC container successfully.");
	}
	
	/**
	 * Get the managed object from the context.
	 */
	public <T> T get(String name, Class<T> t) {
		var result = get(name);
		return result != null ? t.cast(result) : null;
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
	 * Get the managed object without qualifier name.
	 */
	public<T> T get(Class<T> clazz) {
		return get(clazz.getName(), clazz);
	}
	
	/**Export the configuration the container held.*/
	public String getConfig(String key) {
		if(key == null) return null;
		if(configs == null) return null;
		return this.configs.get(key);
	}
	
	/**
	 * Create a bean factory to bind beans manually.
	 */
	public Factory createFactory() {
		var result = new Factory(crafts);
		result.setConfigs(configs);
		return result;
	}
	
	/**
	 * Cache the IOC context into  SERVLET context.
	 */
	public void cache(ServletContext context) {
		context.setAttribute(NAME, this);
	}
}