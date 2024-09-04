package cn.techarts.xkit.ioc;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import javax.inject.Named;

import cn.techarts.xkit.util.Hotchpotch;

/**
 * A craft(REF, KEY, VAL) needs to be inject into host craft.
 */
public class Injector {
	//Null means REF
	private Type type;
	//Null means VAL
	private String name;
	private Object value;
	private boolean assembled;
	
	/**Create a REF object*/
	public static Injector ref(String ref) {
		return new Injector(ref);
	}
	
	/**Create a KEY object*/
	public static Injector key(String key, Class<?> t) {
		var result = new Injector(key);
		result.setType(t != null ? t : Object.class);
		return result;
	}
	
	/**Create a VAL object*/
	public static Injector val(Object val) {
		var result = new Injector();
		result.setValue(val);
		result.setType(val.getClass());
		return result;
	}
	
	Injector() {}
	
	Injector(String name) {
		this.setName(name);
	}
	
	public Injector(Parameter p) {
		var named = p.getAnnotation(Named.class);
		var valued = p.getAnnotation(Valued.class);
		parseAnnotations(named, valued, p.getType());
	}
	
	public Injector(Field f) {
		var named = f.getAnnotation(Named.class);
		var valued = f.getAnnotation(Valued.class);
		parseAnnotations(named, valued, f.getType());
	}
	
	private void  parseAnnotations(Named named, Valued valued, Class<?> clazz) {
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
		this.name = t; //Default Name
		if(v != null) {
			this.name = v.key();
			if(v.val().isBlank()) return;
			value = Hotchpotch.cast(type, v.val());
		}else {
			var tmp = n.value();
			if(!tmp.isBlank()) this.name = tmp;
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public String getName() {
		return name;
	}
	
	/**KEY or VALUE, NOT A REF*/
	public boolean isNotREF() {
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
			throw Panic.configKeyMissing(name);
		}
		//To avoid duplicated setting 
		if(this.value != null) return;
		if(type == null) { //REF
			this.value = value;
		}else {
			this.value = Hotchpotch.cast(value, type);
		}
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean completed() {
		return this.value != null;
	}

	public boolean isAssembled(boolean singleton) {
		return singleton && assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}
}