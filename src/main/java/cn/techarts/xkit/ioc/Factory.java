package cn.techarts.xkit.ioc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import cn.techarts.xkit.util.Helper;

public class Factory {
	private Map<String, Meta> crafts;
	private Map<String, Meta> material;
	private Map<String, String> configs;
	
	public Factory(Map<String, Meta> container) {
		this.crafts = container;
		if(this.crafts == null) {
			throw Panic.nullContainer();
		}
		this.configs = new HashMap<>(64);
		material = new ConcurrentHashMap<>(512);
	}
	
	public void setConfiguration(Map<String, String> configs) {
		if(configs == null) return;
		this.configs = configs;
	}
	
	private void scanJSR330BasedCrafts(String... folders) {
		if(folders == null || folders.length == 0) return;
		for(int i = 0; i < folders.length; i++) {
			this.scanAndResolve(folders[i]);
		}
	}
	
	private void scanConfigBasedCrafts(String... files) {
		if(files == null || files.length == 0) return;
		for(int i = 0; i < files.length; i++) {
			this.parseJsonFile(files[i]);
		}
	}
	
	/**
	 * Support multiple class-paths and JSON files.
	 */
	public void initialize(String[] folders, String[] files) {
		this.scanJSR330BasedCrafts(folders);
		this.scanConfigBasedCrafts(files);
		this.assembleAndInstanceCrafts();
	}
	
	/**
	 * Just support single class-path and JSON file.
	 */
	public void initialize(String folders, String files) {
		this.scanJSR330BasedCrafts(folders);
		this.scanConfigBasedCrafts(files);
		this.assembleAndInstanceCrafts();
	}
	
	public void register(String clzz) {
		var result = toMeta(clzz);
		if(result == null) return;
		material.put(result.getName(), result);
	}
	
	public void register(Node craft) {
		var result = toMeta(craft);
		if(result == null) return;
		material.put(craft.getName(), result);
	}
	
	private Meta toMeta(String className) {
		try {
			var obj = Class.forName(className);
			var ann = obj.getAnnotation(Named.class);
			if(ann == null) return null; //UNMANAGED
			var name = obj.getName();
			if(!ann.value().isEmpty()) {
				name = ann.value(); //Qualifier first
			}
			var tmp = obj.getAnnotation(Singleton.class);
			return new Meta(name, obj, tmp != null);			
		}catch(ClassNotFoundException e) {
			throw Panic.notFound(className);
		}
	}
	
	private Meta toMeta(Node craft) {
		try {
			var obj = Class.forName(craft.getType());
			craft.resetName(obj.getName());
			return new Meta(craft, obj);			
		}catch(ClassNotFoundException e) {
			throw Panic.notFound(craft.getType());
		}
	}
	
	
	private void parseJsonFile(String file){
		if(file == null) return;
		var parser = new ObjectMapper();
		try {
			var json = new File(file);
			var nodes = parser.readValue(json, ArrayNode.class);
			for(var node : nodes) {
				var jb = parser.treeToValue(node, Node.class);
				if(jb != null) this.register(jb);
			}
		}catch(Exception e) {
			throw Panic.failed2ParseJson(file, e);
		}
	}
	
	private void scanAndResolve(String basePackage) {
		if(basePackage == null) return;
		var base = new File(basePackage);
		var start = base.getAbsolutePath().length();
		var classes = Helper.scanClasses(base, start);
		classes.forEach(clazz->this.register(clazz));
	}
	
	private void assembleAndInstanceCrafts() {
		if(material.isEmpty()) return;
		var start = material.size();
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
		var end = material.size();
		if(start == end && end > 0) {
			throw Panic.circularDependence(dump());
		}
		this.assembleAndInstanceCrafts();
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

	public Map<String, String> getConfigs() {
		return configs;
	}

	public void setConfigs(Map<String, String> configs) {
		this.configs = configs;
	}
	
	public String getConfig(String key) {
		if(configs == null) return null;
		var result = configs.get(key);
		if(result != null) return result;
		throw Panic.keyMissing(key);
	}
}