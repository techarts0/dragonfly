package cn.techarts.xkit.ioc;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import javax.inject.Named;

import cn.techarts.xkit.util.Helper;

public class Injector {
	//Null means REF
	private Type type;
	//Null means VAL
	private String name;
	private Object value;
	private boolean assembled;
	
	public static Injector ref(String ref) {
		var result = new Injector();
		result.setName(ref);
		return result;
	}
	
	public static Injector key(String key, Class<?> t) {
		var result = new Injector();
		result.setName(key);
		if(t != null) {
			result.setType(t);
		}else {
			result.setType(Object.class);
		}
		return result;
	}
	
	public static Injector val(Object val) {
		var result = new Injector();
		result.setValue(val);
		result.setType(val.getClass());
		return result;
	}
	
	public Injector() {}
	
	public Injector(Parameter p) {
		var named = p.getAnnotation(Named.class);
		var valued = p.getAnnotation(Valued.class);
		this.prepareArg(named, valued, p.getType());
	}
	
	public Injector(Field f) {
		var named = f.getAnnotation(Named.class);
		var valued = f.getAnnotation(Valued.class);
		this.prepareArg(named, valued, f.getType());
	}
	
	public Injector(Field f, Object val, Class<?> t) {
		
	}
	
	private void  prepareArg(Named named, Valued valued, Class<?> clazz) {
		if(named == null && valued == null) {
			throw Panic.annotationMissing();
		}
		if(named != null && valued != null) {
			throw Panic.annotationConflicted();
		}
		if(valued != null) type = clazz;//NOT A REF
		setName(clazz.getName(), named, valued);
	}
	
	
	private void setName(String t, Named n, Valued v) {
		this.name = t;
		if(v != null) {
			this.name = v.key();
			if(!"".equals(v.val())) {	//VAL
				this.value = Helper.cast(type, v.val());
			}
		}else {
			var tmp = n.value();
			if(!tmp.isEmpty()) {
				this.name = tmp;
			}
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public String getName() {
		return name;
	}
	public boolean isPrimitive() {
		return this.type != null;
	}
	
	public Object getValue() {
		return value;
	}

	//Convert to the target type firstly
	public void setValue(Object value) {
		if(value == null) {
			if(type == null) return; //REF
			if(this.value != null) return; //VAL
			throw Panic.keyMissing(name);
		}
		//To avoid duplicated setting 
		if(this.value != null) return;
		if(type == null) {
			this.value = value;
		}else {
			this.value = Helper.cast(value, type);
		}
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean valid() {
		return this.value != null;
	}

	public boolean isAssembled(boolean singleton) {
		return singleton && assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}
}
