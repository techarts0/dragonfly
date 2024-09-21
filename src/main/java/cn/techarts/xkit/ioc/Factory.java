package cn.techarts.xkit.ioc;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.techarts.xkit.util.Hotpot;

public class Factory {
	private Map<String, Craft> crafts;
	private Map<String, Craft> material;
	private Map<String, String> configs;
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public Factory(Map<String, Craft> container) {
		if(container == null) {
			throw Panic.nullContainer();
		}
		this.crafts = container;
		this.configs = new HashMap<>(64);
		material = new ConcurrentHashMap<>(512);
	}
	
	public void setConfigs(Map<String, String> configs) {
		this.configs = configs == null ? Map.of() : configs;
	}
	
	public Map<String, String> getConfigs() {
		return configs;
	}
	
	public String getConfig(String key) {
		if(configs == null) return null;
		var result = configs.get(key);
		if(result != null) return result;
		throw Panic.configKeyMissing(key);
	}
	
	private void resolveJSR330BasedCrafts(String... classpath) {
		if(classpath == null || classpath.length == 0) return;
		for(int i = 0; i < classpath.length; i++) {
			this.scanAndResolveCrafts(classpath[i]);
		}
	}
	
	private void resolveConfigBasedCrafts(String... resources) {
		if(resources == null || resources.length == 0) return;
		for(int i = 0; i < resources.length; i++) {
			this.parseAndResolveXMLCrafts(resources[i]);
		}
	}
	
	/**
	 * Support multiple class-paths and XML files.
	 */
	public void start(String[] classpaths, String[] resources) {
		this.resolveJSR330BasedCrafts(classpaths);
		this.resolveConfigBasedCrafts(resources);
		this.assembleAndInstanceManagedCrafts();
		LOGGER.info("Assembled " + crafts.size() + " managed objects.");
	}
	
	/**
	 * Just support single class-path and JSON/XML file.
	 */
	public void start(String classpath, String resource) {
		this.resolveJSR330BasedCrafts(classpath);
		this.resolveConfigBasedCrafts(resource);
		this.assembleAndInstanceManagedCrafts();
		LOGGER.info("Assembled " + crafts.size() + " managed objects.");
	}
	
	/**
	 * Append a managed bean instance into IOC container.
	 */
	public void bind(Object... beans) {
		if(beans == null) return;
		if(beans.length == 0) return;
		for(var bean : beans) {
			if(bean != null) {
				this.register(bean);
			}
		}
		assembleAndInstanceManagedCrafts();
	}
	
	/**
	 * Append a managed bean into IOC container by class.
	 */
	public void bind(Class<?>... beans) {
		if(beans == null) return;
		if(beans.length == 0) return;
		for(var bean : beans) {
			if(bean != null) {
				this.register(bean);
			}
		}
		assembleAndInstanceManagedCrafts();
	}
	
	/**
	 * Append a managed bean into IOC container by class name.
	 */
	public void bind(String... classes) {
		if(classes == null) return;
		if(classes.length == 0) return;
		for(var clazz : classes) {
			if(clazz != null) {
				this.register(clazz);
			}
		}
		assembleAndInstanceManagedCrafts();
	}
	
	public void register(String clzz) {
		var result = toCraft(clzz);
		if(result == null) return;
		if(!result.isManaged()) return;
		material.put(result.getName(), result);
	}
	
	private void register(Object bean) {
		if(bean == null) return;
		var craft = toCraft(bean.getClass());
		craft.setInstance(bean);
		material.put(craft.getName(), craft);
	}

	public void register(Class<?> clazz) {
		if(clazz == null) return;
		var craft = toCraft(clazz);
		material.put(craft.getName(), craft);
	}
	
	public void register(Craft craft) {
		material.put(craft.getName(), craft);
	}
	
	private Craft toCraft(Class<?> clazz) {
		if(!Hotpot.newable(clazz)) return null;
		var named = clazz.getAnnotation(Named.class);
		var s = clazz.isAnnotationPresent(Singleton.class);
		var explicitly = named != null || s;
		if(explicitly == false) return null;
		//Bean id: the qualifier name is first
		var name = named != null ? named.value() : ""; 
		if(name.isEmpty()) name = clazz.getName();
		return new Craft(name, clazz, s, explicitly);			
	}
	
	private Craft toCraft(String className) {
		try {
			return toCraft(Class.forName(className));
		}catch(ClassNotFoundException e) {
			throw Panic.classNotFound(className, e);
		}
	}
	
	/**Crafts defined in XML file.*/
	private void parseAndResolveXMLCrafts(String resource){
		try {
			var factory = DocumentBuilderFactory.newInstance();
			var stream = new FileInputStream(resource);
			var doc = factory.newDocumentBuilder().parse(stream);
	        doc.getDocumentElement().normalize();
	        var crafts = doc.getElementsByTagName("bean");
	        if(crafts == null || crafts.getLength() == 0) return;
	        for(int i = 0; i < crafts.getLength(); i++) {
	        	register(xmlBean2Craft(crafts.item(i)));
	        }
		}catch(Exception e) {
			throw Panic.failed2ParseJson(resource, e);
		}
	}
	
	private void scanAndResolveCrafts(String classpath) {
		if(classpath == null || classpath.isBlank()) return;
		var base = new File(classpath);//Root class-path
		if(base == null || !base.isDirectory()) return;
		var start = base.getAbsolutePath().length();
		var classes = Hotpot.scanClasses(base, start);
		classes.forEach(clazz->this.register(clazz));
	}
	
	private void assembleAndInstanceManagedCrafts() {
		var start = material.size();
		if(start == 0) return; //Assemble Completed
		for(var entry : material.entrySet()) {
			var craft = entry.getValue();
			craft.inject(crafts, configs);
			craft.instance().assemble();
			if(craft.isAssembled()) {
				var key = entry.getKey();
				this.crafts.put(key, craft);
				this.material.remove(key);
			}
		}
		if(start == material.size()){ //Not Empty
			throw Panic.circularDependence(dump());
		}
		this.assembleAndInstanceManagedCrafts();
	}
	
	private String dump() {
		var result = new StringBuilder();
		material.keySet().forEach(key->{
			result.append(key).append(", ");
		});
		var start = result.length() - 2;
		result.delete(start, start + 2);
		return result.toString();
	}
	
	private void parseArgs(NodeList args, Craft result) {
		if(args == null || args.getLength() != 1) return;
		var first = (org.w3c.dom.Element)args.item(0);
		args = first.getElementsByTagName("arg");
		if(args == null || args.getLength() == 0) return;
		for(int i = 0; i < args.getLength(); i++) {
			var arg = args.item(i);
			if(arg.getNodeType() != Node.ELEMENT_NODE) continue;
			var injector = xmlNode2Injector((Element)arg);
			result.addArgument(i, injector);
		}
	}
	
	private void parseProps(NodeList props, Craft result) {
		if(props == null || props.getLength() != 1) return;
		var first = (org.w3c.dom.Element)props.item(0);
		props = first.getElementsByTagName("prop");
		if(props == null || props.getLength() == 0) return;
		var fields = new HashMap<String, Field>();
		getFields(fields, Hotpot.forName(result.getType()));
		for(int i = 0; i < props.getLength(); i++) {
			var prop = props.item(i);
			if(prop.getNodeType() != Node.ELEMENT_NODE) continue;
			var tmp = (Element)prop;
			var name = tmp.getAttribute("name");
			var injector = xmlNode2Injector(tmp);
			result.addProperty(fields.get(name), injector);
		}
	}
	
	private Injectee xmlNode2Injector(Element node) {
		var ref = node.getAttribute("ref");
		var key = node.getAttribute("key");
		var val = node.getAttribute("val");
		var type = node.getAttribute("type");
		return Injectee.of(ref, key, val, type);
	}
	
	private Craft xmlBean2Craft(Node node) {
		if(node.getNodeType() != Node.ELEMENT_NODE) return null;
		var craft = (org.w3c.dom.Element)node;
		var result = new Craft(craft.getAttribute("type"));
		result.setName(craft.getAttribute("id"));
		result.setSingleton(craft.getAttribute("singleton"));
		parseArgs(craft.getElementsByTagName("args"), result);
		parseProps(craft.getElementsByTagName("props"), result);
		return result.withConstructor();
	}
	
	private void getFields(Map<String, Field> result, Class<?> clazz) {
		if(clazz == null) return; //Without super class
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				result.put(f.getName(), f);
			}
		}
		getFields(result, clazz.getSuperclass());
	}
}