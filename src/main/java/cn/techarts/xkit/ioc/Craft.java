package cn.techarts.xkit.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import cn.techarts.xkit.util.Converter;
import cn.techarts.xkit.util.Hotpot;

public class Craft {
	private String name;
	private String type;
	private Object instance;
	private boolean singleton;
	private boolean assembled;
	private boolean explictly;
		
	/** Injected or default constructor*/
	private Constructor<?> constructor;
	
	/** Injected Constructor Arguments*/
	private Map<Integer, Injector> arguments;
	
	/** Injected Fields(Values)*/
	private Map<Field, Injector> properties;
	
	public Craft(String type) {
		this.type = type;
		this.explictly = true;
		this.arguments = new HashMap<>();
		this.properties = new HashMap<>();	
		var clazz = Hotpot.forName(type);
		this.resolveInjectedFields(clazz); //Annotation
	}
	
	/**From Annotation*/
	public Craft(String name, Class<?> clazz, boolean singleton, boolean explictly) {
		this.name = name;
		this.singleton = singleton;
		this.explictly = explictly;
		this.arguments = new HashMap<>();
		this.properties = new HashMap<>();
		this.resolveInjectedFields(clazz);
		this.resolveInjectedContructor(clazz);
	}
	
	public boolean isManaged() {
		if(explictly) return true;
		if(!arguments.isEmpty()) {
			return true;
		}
		return !properties.isEmpty();
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
			if(arg.isKEY()) {
				arg.setValue(configs.get(arg.getName()));
			}else {
				var craft = crafts.get(arg.getName());
				if(craft != null) arg.setValue(craft.getInstance());
			}
		}
	}
	
	//Set REF and KEY (VAL set already)
	private void setPropertiesDependences(Map<String, Craft> crafts, Map<String, String> configs) {
		for(var field : properties.values()) {
			if(field.completed()) continue; //The value set already.
			if(field.isKEY()) { //Key here
				var v = configs.get(field.getName());
				if(v == null) {
					throw Panic.configKeyMissing(field.getName());
				}
				field.setValue(Hotpot.cast(field.getType(), v));
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
	
	public Craft withConstructor() {
		var clazz = Hotpot.forName(type);
		var cons = clazz.getConstructors();
		if(cons == null || cons.length == 0) {
			throw Panic.noDefaultConstructor(clazz);
		}
		for(var c : cons) {
			var parameterMatched = true;
			var args = c.getParameters();
			if(args.length != arguments.size()) continue;
			
			for(int i = 0; i < args.length; i++) {
				var a = args[i].getType().getName();
				var e = arguments.get(i).getTypeName();
				if(!Hotpot.compareTypes(a, e)) {
					parameterMatched = false; break;
				}
			}
			if(parameterMatched) { 
				this.constructor = c; break;
			}
		}
		
		if(this.constructor == null) {
			try { //Default and public constructor
				this.constructor = clazz.getConstructor();
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
		return this;
	}
	
	private void resolveInjectedContructor(Class<?> clazz) {
		var cons = clazz.getConstructors();
		if(cons == null || cons.length == 0) return;
		
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
		
		if(this.constructor == null && explictly) {
			try { //Default and public constructor
				this.constructor = clazz.getConstructor();
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
	}
	
	private void resolveInjectedFields(Class<?> clazz) {
		if(clazz == null) return;
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				if(f.isAnnotationPresent(Inject.class)) {
					this.addProperty(f, new Injector(f));
				}
			}
		}
		resolveInjectedFields(clazz.getSuperclass());
	}

	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
	public void setSingleton(String singleton) {
		this.singleton = Converter.toBoolean(singleton);
	}
	
	public void addArgument(int index, Injector arg) {
		this.arguments.put(index, arg);
	}
	
	public void addProperty(Field field, Injector arg) {
		if(field == null || arg == null) return;
		arg.setType(field.getType());
		this.properties.put(field, arg);
		var val = arg.getValue();
		if(val == null) return; 
		arg.resetValue(Hotpot.cast(val, field.getType()));
	}
}