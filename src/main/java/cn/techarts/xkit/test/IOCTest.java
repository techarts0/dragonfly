package cn.techarts.xkit.test;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Test;

import cn.techarts.xkit.ioc.Craft;
import cn.techarts.xkit.ioc.Context;
import cn.techarts.xkit.ioc.Factory;
import cn.techarts.xkit.ioc.Valued;
import cn.techarts.xkit.util.Helper;
import junit.framework.TestCase;

public class IOCTest {
	
	//@Test
	public void testBeanMeta() {
		var meta = new Craft("person", Person.class, false);
	}
	
	//@Test
	public void scanClassFolder() {
		var base = "D:/Studio/Project/Java/xmesh2/target/classes";
		var dest = new File(base);
		var start = dest.getAbsolutePath().length();
		var files = Helper.scanClasses(dest, start);
		files.forEach(f->System.out.println(f));
	}
	
	//@Test
	public void initBeanPool() {
		var json = "D:\\Studio\\Project\\Java\\dragonfly\\src\\main\\webapp\\WEB-INF\\crafts-bak.json";
		var base = "D:\\Studio\\Project\\Java\\xkit\\target\\classes";
		var context = Context.make(base, json, Map.of("user.age", "500"));
		
		var person = context.get("Person", Person.class);
		var money = context.get("Money", Money.class);
		TestCase.assertNotNull(person);
		TestCase.assertEquals(person.getName(), "Xiao Zhong");
		TestCase.assertEquals(person.getContact().getId(), 5);
		TestCase.assertEquals(money.getDecemal(), 500);
		TestCase.assertEquals(person.getContact().getSalary().getDecemal(), 500);
		var person2 = context.get("Person", Person.class);
		TestCase.assertEquals(person.hashCode(), person2.hashCode());
	}
	
	//@Test
	public void testParseJson() {
		var json = "D:\\Studio\\Project\\Java\\dragonfly\\src\\main\\webapp\\WEB-INF\\beans.json";
		
	}
	
	@Test
	public void testReflectFields() {
		Class<?> clz = String.class;
		Class<?> smp = int.class;
		Character c;
		TestCase.assertEquals(true, clz == String.class);
		TestCase.assertEquals(true, smp.isPrimitive());
	}
	
	private void reflectObject(Class<?> obj) {
		
		
	}
	
}