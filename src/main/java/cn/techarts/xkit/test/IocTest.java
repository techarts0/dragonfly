package cn.techarts.xkit.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Test;

import cn.techarts.xkit.ioc.Context;
import cn.techarts.xkit.ioc.ProviderImpl;
import junit.framework.TestCase;

public class IocTest {
	private static final String BASE = "/D:/Studio/Project/Java/dragonfly/target/classes/";
	private static final String XML = "D:\\Studio\\Project\\Java\\dragonfly\\src\\main\\java\\cn\\techarts\\xkit\\test\\beans.xml";
	
	//@Test
	public void testDiContainer() {
		var ctx = Context.make(BASE, XML, Map.of("zone", "+86", "user.id", "45"));
		var p = ctx.get("person", Person.class);
		var m = p.getMobile();
		TestCase.assertEquals(p.getId(), 45);
		TestCase.assertEquals(m.getZone(), "+86");
		TestCase.assertEquals(p.getName(), "Jeff Dean");
		TestCase.assertEquals(m.getNumber(), "13980092699");
	}
	
	//@Test
	public void testBinding() {
		var ctx = Context.make(Map.of("zone", "+86", "user.id", "45"));
		ctx.bind(Person.class, Mobile.class);
		var m = ctx.get(Person.class).getMobile();
		TestCase.assertEquals("+86", m.getZone());
		TestCase.assertEquals("13980092699", m.getNumber());
	}
	
	@Test
	public void testProvider() {
		var ctx = Context.make(Map.of("zone", "+86", "user.id", "45"));
		ctx.bind(Office.class, Mobile.class, Person.class);
		var m = ctx.get(Person.class).getMobile();
		TestCase.assertEquals("+86", m.getZone());
		var o = ctx.get(Office.class).getMobile();
		TestCase.assertEquals("13980092699", o.getNumber());
	}
	
	//@Test
	public void testGenericType() {
		
		var scu = new SomeClassUser(new SomeClass<>());
	}
}

class SomeClassUser{
	public SomeClassUser(SomeInterface<String> name) {
		System.out.println(name.get());
	}
}

class SomeClass<T> implements SomeInterface<T>{
    public T get() {
    	Type typeArgument = ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    	if (!(typeArgument instanceof TypeVariable)) return null;
    	TypeVariable<?> typeVariable = (TypeVariable<?>) typeArgument;
        Type bound = typeVariable.getBounds()[0];
        if (!(bound instanceof Class<?>)) return null; 
        Class<?> clazz = (Class<?>) bound;
        try {
        	var key = clazz.getName();
        	Object obj = "here you are";
        	return (T)clazz.cast(obj);
        }catch (Exception e) {
            throw new RuntimeException("Unable to create instance of type: " + clazz.getName(), e);
        } 
    }
}

interface SomeInterface<T>{
	public T get();
}
