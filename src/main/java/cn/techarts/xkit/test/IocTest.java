package cn.techarts.xkit.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
	
	@Test
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
    	return null;
    }
	
	public void printType() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            System.out.println("T is: " + actualType);
        }
    }
}

interface SomeInterface<T>{
	public T get();
}
