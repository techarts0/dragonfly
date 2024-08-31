package cn.techarts.xkit.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import cn.techarts.xkit.util.Helper;

public class Meta {
	private String name;
	private Object instance;
	private boolean singleton;
	private boolean assembled;
		
	/**Injected or default constructor*/
	private Constructor<?> constructor;
	
	/**Injected Contractor Arguments*/
	private Map<Integer, Injector> injectedArgs;
	
	/**Injected Fields*/
	private Map<Field, Injector> injectedFields;
	
	public Meta(String name, Class<?> clazz, boolean singleton) {
		this.name = name;
		this.singleton = singleton;
		this.injectedArgs = new HashMap<>();
		this.injectedFields = new HashMap<>();
		this.resolveInjectedFields(clazz);
		this.resolveInjectedContructor(clazz);
	}
	
	public Meta(Node node, Class<?> clazz) {
		this.name = node.getName();
		this.singleton = node.isSingleton();
		this.injectedArgs = new HashMap<>();
		this.injectedFields = new HashMap<>();
		resolveInjectedFields(node.getProps(), clazz);
		resolveInjectedContructor(node.getArgs(), clazz);
	}
	
	public void inject(Map<String, Meta> crafts, Map<String, String> configs) {
		setConstructorDependences(crafts, configs);
		setPropertiesDependences(crafts, configs);
	}
	
	private void setConstructorDependences(Map<String, Meta> crafts, Map<String, String> configs) {
		if(this.instance != null) return; //Set dependences completed
		var len = injectedArgs.size();
		for(int i = 0; i < len; i++) {
			var arg = injectedArgs.get(Integer.valueOf(i));
			if(arg.valid()) continue; //The value set already.
			if(arg.isPrimitive()) {
				arg.setValue(configs.get(arg.getName()));
			}else {
				var craft = crafts.get(arg.getName());
				if(craft != null) arg.setValue(craft.getInstance());
			}
		}
	}
	
	private void setPropertiesDependences(Map<String, Meta> crafts, Map<String, String> configs) {
		for(var arg : injectedFields.values()) {
			if(arg.valid()) continue; //The value set already.
			if(arg.isPrimitive()) {
				arg.setValue(configs.get(arg.getName()));
			}else {
				var craft = crafts.get(arg.getName());
				if(craft != null) arg.setValue(craft.getInstance());
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
			this.instance();
			this.assemble();
			return instance;
		}
	}
	public void setInstance(Object instance) {
		this.instance = instance;
	}
	public boolean isSingleton() {
		return singleton;
	}
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}	
	public Map<Integer, Injector> getInjectedArgs(){
		return this.injectedArgs;
	}
	public Map<Field, Injector> getInjectedFields(){
		return this.injectedFields;
	}
	private Object[] getConstructorArgs() {
		var len = injectedArgs.size();
		var result = new Object[len];
		for(int i = 0; i < len; i++) {
			var key = Integer.valueOf(i);
			var arg = injectedArgs.get(key);
			if(!arg.valid()) return null;
			result[i] = arg.getValue();
		}
		return result;
	}
	
	/**
	 * Create an instance but the fields are not injected.
	 */
	public Meta instance() {
		if(instance != null && singleton) return this;
		try {
			if(isDefConstructor()) {
				instance = constructor.newInstance();
			}else {
				var params = getConstructorArgs();
				if(params == null) return this; //Waiting...
				instance = constructor.newInstance(params);
			}
			this.assembled = this.injectedFields.isEmpty();
			return this; //To support chain-style calling
		}catch(Exception e) {
			throw Panic.cannotInstance(this.name, e);
		}
	}
	
	
	public void assemble() {
		if(this.assembled) return;
		if(instance == null) return; //Waiting...
		this.assembled = true; //Suppose Completed
		for(var entry : injectedFields.entrySet()) {
			var arg = entry.getValue();
			if(arg.isAssembled(singleton)) continue; //Used
			if(!arg.valid()) { //Waiting...
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
	}
	
	public boolean isDefConstructor() {
		return injectedArgs == null || injectedArgs.isEmpty();
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
		var constructors = clazz.getConstructors();
		if(constructors == null) {
			throw Panic.noDefaultConstructor(clazz, null);
		}
		
		for(var c : constructors) {
			if(!c.isAnnotationPresent(Inject.class)) continue;
			this.constructor = c; //Cache it for new instance
			var args = this.constructor.getParameters();
			if(args == null || args.length == 0) break;
			
			for(int i = 0; i < args.length; i++) {
				var arg = new Injector(args[i]);
				injectedArgs.put(Integer.valueOf(i), arg);
			}
			break; //Only ONE constructor can be injected
		}
		
		if(this.constructor == null) {
			try {
				this.constructor = clazz.getConstructor();
				//this.constructor = clazz.getDeclaredConstructor();
				//this.constructor.setAccessible(true); //If private
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
	}
	
	private void resolveInjectedContructor(List<Object> args, Class<?> clazz) {
		var constructors = clazz.getConstructors();
		if(constructors == null) {
			throw Panic.noDefaultConstructor(clazz, null);
		}
		
		if(args != null && !args.isEmpty()) {
			for(var c : constructors) {
				var cargs = c.getParameters();
				if(args.size() != cargs.length) continue;
				boolean parameterMatched = true;
				for(int i = 0; i < cargs.length; i++) {
					var a = getType(args.get(i));
					var e = cargs[i].getType().getName();
					if(!Helper.compareTypes(a, e)) { //Expect, Actual
						parameterMatched = false; break;
					}
				}
				if(parameterMatched == false) continue; //Next
				
				for(int i = 0; i < cargs.length; i++) {
					injectedArgs.put(i, parse(args.get(i), null));
				}
				this.constructor = c; //Cache it for new instance
				break; //Only ONE constructor can be injected
			}
		}
		
		if(this.constructor == null) {
			try {
				this.constructor = clazz.getConstructor();
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
	}
	
	private void resolveInjectedFields(Map<String, Object> props, Class<?> clazz) {
		if(props == null || props.isEmpty()) return;
		if(clazz == null) return; //Without super class
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length >= 0) {
			for(var f : fs) {
				var val = props.get(f.getName());
				if(val == null) continue;
				injectedFields.put(f, parse(val, f.getType()));
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
		if(fs != null && fs.length > 0) {
			for(var f : fs) {
				if(f.isAnnotationPresent(Inject.class)) {
					injectedFields.put(f, new Injector(f));
				}
			}
		}
		resolveInjectedFields(clazz.getSuperclass());
	}
}