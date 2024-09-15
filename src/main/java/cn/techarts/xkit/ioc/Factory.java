package cn.techarts.xkit.ioc;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
		if(configs == null) return;
		this.configs = configs;
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
			var file = resources[i];
			if(file.endsWith(".xml")) {
				this.parseAndResolveXMLCrafts(file);
			}else if(file.endsWith(".json")) {
				this.parseAndResolveJsonCrafts(file);
			}else {
				throw Panic.unsupportedFileType(file);
			}
		}
	}
	
	/**
	 * Support multiple class-paths and JSON/XML files.
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
	
	public void register(String clzz) {
		var result = toCraft(clzz);
		if(result == null) return;
		material.put(result.getName(), result);
	}
	
	public void register(Element craft) {
		if(craft == null) return;
		var result = toCraft(craft);
		if(result == null) return;
		material.put(craft.getName(), result);
	}
	
	private Craft toCraft(String className) {
		try {
			var obj = Class.forName(className);
			var named = obj.getAnnotation(Named.class);
			if(named == null) return null; //UNMANAGED
			var name = named.value(); //Qualifier first
			if(name.isEmpty()) name = className;
			var s = obj.isAnnotationPresent(Singleton.class);
			return new Craft(name, obj, s);			
		}catch(ClassNotFoundException e) {
			throw Panic.classNotFound(className, e);
		}
	}
	
	private Craft toCraft(Element craft) {
		try {
			return new Craft(craft, craft.instance());			
		}catch(ClassNotFoundException e) {
			throw Panic.classNotFound(craft.getType(), e);
		}
	}
	
	/**Crafts defined in JSON file.*/
	private void parseAndResolveJsonCrafts(String resource){
		if(resource == null) return;
		var parser = new ObjectMapper();
		try {
			var stream = new FileInputStream(resource);
			var nodes = parser.readValue(stream, ArrayNode.class);
			if(nodes == null || nodes.isEmpty()) return;
			for(var node : nodes) {
				register(parser.treeToValue(node, Element.class));
			}
		}catch(Exception e) {
			throw Panic.failed2ParseJson(resource, e);
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
	        	register(xmlNode2Element(crafts.item(i)));
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
	
	private Element xmlNode2Element(Node node) {
		if(node.getNodeType() != Node.ELEMENT_NODE) return null;
		var result = new Element();
		var craft = (org.w3c.dom.Element)node;
		result.setName(craft.getAttribute("id"));
		result.setType(craft.getAttribute("type"));
		result.setSingleton(craft.getAttribute("singleton"));
		
		var args = craft.getElementsByTagName("args");
		if(args != null && args.getLength() == 1) {
			var first = (org.w3c.dom.Element)args.item(0);
			args = first.getElementsByTagName("arg");
			if(args != null && args.getLength() > 0) {
				for(int i = 0; i < args.getLength(); i++) {
					var arg = args.item(i);
					if(arg.getNodeType() == Node.ELEMENT_NODE) {
						var tmp = (org.w3c.dom.Element)arg;
						var ref = tmp.getAttribute("ref");
						var key = tmp.getAttribute("key");
						var val = tmp.getAttribute("val");
						var type = tmp.getAttribute("type");
						result.addArg(ref, key, val, type);
					}
				}
			}
		}
		var props = craft.getElementsByTagName("props");
		if(props != null && props.getLength() == 1) {
			var first = (org.w3c.dom.Element)props.item(0);
			props = first.getElementsByTagName("prop");
			if(props != null && props.getLength() > 0) {
				for(int i = 0; i < props.getLength(); i++) {
					var prop = props.item(i);
					if(prop.getNodeType() == Node.ELEMENT_NODE) {
						var tmp = (org.w3c.dom.Element)prop;
						var ref = tmp.getAttribute("ref");
						var key = tmp.getAttribute("key");
						var val = tmp.getAttribute("val");
						var type = tmp.getAttribute("type");
						var name = tmp.getAttribute("name");
						result.addProp(ref, key, val, name, type);
					}
				}
			}
		}
		return result;
	}	
}