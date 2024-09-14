package cn.techarts.xkit.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import cn.techarts.xkit.util.Hotpot;

public class Craft {
	private String name;
	private Object instance;
	private boolean singleton;
	private boolean assembled;
		
	/**Injected or default constructor*/
	private Constructor<?> constructor;
	
	/**Constructor Injectors (Arguments)*/
	private Map<Integer, Injector> arguments;
	
	/**Fields Injector(Values)*/
	private Map<Field, Injector> properties;
	
	/**From Annotation*/
	public Craft(String name, Class<?> clazz, boolean singleton) {
		this.name = name;
		this.singleton = singleton;
		this.arguments = new HashMap<>();
		this.properties = new HashMap<>();
		this.resolveInjectedFields(clazz);
		this.resolveInjectedContructor(clazz);
	}
	
	/**From JSON file*/
	public Craft(Element node, Class<?> clazz) {
		this.name = node.getName();
		this.singleton = node.isSingleton();
		this.arguments = new HashMap<>();
		this.properties = new HashMap<>();
		
		//Allows both of annotation and JSON. 
		resolveInjectedFields(clazz); //Annotation
		resolveInjectedFields(node.getProps(), clazz);
		resolveInjectedContructor(node.getArgs(), clazz);
	}
	
	/**
	 * Set dependent crafts (REF, KEY and VAL) before assembling.
	 */
	public void inject(Map<String, Craft> crafts, Map<String, String> configs) {
		setConstructorDependences(crafts, configs);
		setPropertiesDependences(crafts, configs);
	}
	
	//If the instance is not NULL, that means the craft is assembled successfully.
	private void setConstructorDependences(Map<String, Craft> crafts, Map<String, String> configs) {
		if(instance != null) return; //Set dependences completed
		for(int i = 0; i < arguments.size(); i++) {
			var arg = arguments.get(Integer.valueOf(i));
			if(arg.completed()) continue; //The value set already.
			if(arg.isNotREF()) {
				arg.setValue(configs.get(arg.getName()));
			}else {
				var craft = crafts.get(arg.getName());
				if(craft != null) arg.setValue(craft.getInstance());
			}
		}
	}
	
	private void setPropertiesDependences(Map<String, Craft> crafts, Map<String, String> configs) {
		for(var field : properties.values()) {
			if(field.completed()) continue; //The value set already.
			if(field.isNotREF()) {
				field.setValue(configs.get(field.getName()));
			}else {
				var craft = crafts.get(field.getName());
				if(craft != null) field.setValue(craft.getInstance());
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Object getInstance() {
		if(singleton) {
			return instance;
		}else {
			return instance().assemble();
		}
	}
	
	private Object[] toParameters() {
		var len = arguments.size();
		var result = new Object[len];
		for(int i = 0; i < len; i++) {
			var key = Integer.valueOf(i);
			var arg = arguments.get(key);
			if(!arg.completed()) return null;
			result[i] = arg.getValue();
		}
		return result;
	}
	
	/**
	 * Create an instance but the fields are not injected.
	 */
	public Craft instance() {
		if(instance != null && singleton) return this;
		try {
			if(isDefaultConstructor()) {
				instance = constructor.newInstance();
			}else {
				var params = toParameters();
				if(params == null) return this; //Waiting...
				instance = constructor.newInstance(params);
			}
			//Support constructor and field injection mean time.
			this.assembled = this.properties.isEmpty();
			return this; //Just for supporting chain-style calling
		}catch(Exception e) {
			throw Panic.cannotInstance(this.name, e);
		}
	}
	
	public Object assemble() {
		if(this.assembled) return instance;
		if(instance == null) return null; //Waiting...
		this.assembled = true; //Suppose Completed
		for(var entry : properties.entrySet()) {
			var arg = entry.getValue();
			if(arg.isAssembled(singleton)) continue; //Used
			if(!arg.completed()) { //Waiting...
				this.assembled = false; continue;
			}			
			try {
				var f = entry.getKey();
				var a = f.canAccess(instance);
				if(!a) f.setAccessible(true);
				f.set(instance, arg.getValue());
				arg.setAssembled(true); //Ignored Next
				if(!a) f.setAccessible(false);
			}catch(Exception e) {
				throw Panic.cannotSetFieldValue(e);
			}
		}
		return this.instance; //Just for chain-style calling
	}
	
	public boolean isDefaultConstructor() {
		return arguments == null || arguments.isEmpty();
	}
	
	public Constructor<?> getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	public boolean isAssembled() {
		return assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}
	
	private void resolveInjectedContructor(Class<?> clazz) {
		var cons = clazz.getConstructors();
		if(cons == null || cons.length == 0) {
			throw Panic.noDefaultConstructor(clazz);
		}
		
		for(var c : cons) {
			if(!c.isAnnotationPresent(Inject.class)) continue;
			this.constructor = c; //Cache it for new instance
			var args = this.constructor.getParameters();
			if(args == null || args.length == 0) break;
			
			for(int i = 0; i < args.length; i++) {
				var arg = new Injector(args[i]);
				arguments.put(Integer.valueOf(i), arg);
			}
			break; //Only ONE constructor can be injected
		}
		
		if(this.constructor == null) {
			try { //Default and public constructor
				this.constructor = clazz.getConstructor();
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
	}
	
	private void resolveInjectedContructor(List<Object> args, Class<?> clazz) {
		var constructors = clazz.getConstructors();
		if(constructors == null) {
			throw Panic.noDefaultConstructor(clazz);
		}
		if(args == null || args.isEmpty()) {
			this.setDefaultConstructor(clazz); return;
		}
		
		for(var c : constructors) {
			if(constructor != null) break;
			var params = c.getParameters();
			if(args.size() != params.length) continue;
			boolean parameterMatched = true;
			for(int i = 0; i < params.length; i++) {
				var a = getType(args.get(i));
				var e = params[i].getType().getName();
				if(!Hotpot.compareTypes(a, e)) {//Expect, Actual
					parameterMatched = false; break;
				}
			}
			if(parameterMatched == false) continue; //Next
				
			for(int i = 0; i < params.length; i++) {
				arguments.put(i, parse(args.get(i), null));
			}
			this.constructor = c; //Cache it for new instance
		}
		
		if(this.constructor == null) setDefaultConstructor(clazz);
	}
	
	private void setDefaultConstructor(Class<?> clazz) {
		if(this.constructor != null) return;
		try {
			this.constructor = clazz.getConstructor();
		}catch(NoSuchMethodException | SecurityException es) {
			throw Panic.noDefaultConstructor(clazz, es);
		}
	}
	
	private void resolveInjectedFields(Map<String, Object> props, Class<?> clazz) {
		if(clazz == null) return; //Without super class
		if(props == null || props.isEmpty()) return;
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				var val = props.get(f.getName());
				if(val == null) continue;
				properties.put(f, parse(val, f.getType()));
			}
		}
		resolveInjectedFields(props, clazz.getSuperclass());
	}
	
	//REF:User:cn.techarts.xkit.test.User
	private String getType(Object arg) {
		if(!(arg instanceof String)) {
			return arg.getClass().getName();
		}
		var tmp = ((String)arg).trim();
			
		if(tmp.startsWith("REF:") ||
			tmp.startsWith("KEY:")) {
			var idx = tmp.lastIndexOf(':');
			return tmp.substring(idx + 1);
		}else {
			return tmp.getClass().getName();
		}
	}
	
	private Injector parse(Object arg, Class<?> ft) {
		if(!(arg instanceof String)) {
			return Injector.val(arg);
		}
		var tmp = ((String)arg).trim();
		if(tmp.startsWith("REF:")) {
			var idx = tmp.lastIndexOf(':');
			if(idx == 3) idx = tmp.length();
			var val = tmp.substring(4, idx);
			return Injector.ref(val);
		}else if(tmp.startsWith("KEY:")) {
			var v = tmp.substring(4);
			return Injector.key(v, ft);
		}else { //Default VAL
			return Injector.val(tmp);
		}
	}
	
	private void resolveInjectedFields(Class<?> clazz) {
		if(clazz == null) return;
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				if(f.isAnnotationPresent(Inject.class)) {
					properties.put(f, new Injector(f));
				}
			}
		}
		resolveInjectedFields(clazz.getSuperclass());
	}
}