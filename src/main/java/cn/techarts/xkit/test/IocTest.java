package cn.techarts.xkit.test;

import java.util.Map;

import org.junit.Test;

import cn.techarts.xkit.ioc.Context;
import junit.framework.TestCase;

public class IocTest {
	private static final String BASE = "/D:/Studio/Project/Java/dragonfly/target/classes/";
	private static final String XML = "D:\\Studio\\Project\\Java\\dragonfly\\src\\main\\java\\cn\\techarts\\xkit\\test\\beans.xml";
	
	@Test
	public void testDiContainer() {
		var ctx = Context.make(BASE, XML, Map.of("zone", "+86", "user.id", "45"));
		var p = ctx.get("person", Person.class);
		var m = p.getMobile();
		TestCase.assertEquals(p.getId(), 45);
		TestCase.assertEquals(m.getZone(), "+86");
		TestCase.assertEquals(p.getName(), "Jeff Dean");
		TestCase.assertEquals(m.getNumber(), "13980092699");
	}
}
