package cn.techarts.xkit.test;

import java.util.Map;
import org.junit.Test;
import cn.techarts.xkit.ioc.Context;
import cn.techarts.xkit.util.Scanner;
import junit.framework.TestCase;

public class IocTest {
	private static final String BASE = "/D:/Studio/Project/Java/dragonfly/target/classes/";
	private static final String XML = "D:\\Studio\\Project\\Java\\dragonfly\\src\\main\\java\\cn\\techarts\\xkit\\test\\beans.xml";
	
	//@Test
	public void testDiContainer() {
		var ctx = Context.make(Map.of("zone", "+86", "user.id", "45", "build.name", "Library"));
		ctx.createFactory().scan(BASE).parse(XML).start();
		var p = ctx.get("person", Person.class);
		var m = p.getMobile();
		TestCase.assertEquals(p.getId(), 45);
		TestCase.assertEquals(m.getZone(), "+86");
		TestCase.assertEquals(p.getName(), "Jeff Dean");
		TestCase.assertEquals(m.getNumber(), "13980092699");
	}
	
	@Test
	public void testProvider() {
		var ctx = Context.make(Map.of("zone", "+86", "user.id", "45", "build.name", "Library"));
		var factory = ctx.createFactory();
		factory.register(Person.class, Mobile.class).register(Office.class).start();
		var p = ctx.get(Person.class);
		var m = ctx.get(Mobile.class);
		var o = ctx.get(Office.class);
		
		TestCase.assertEquals("+86", m.getZone());
		TestCase.assertEquals(45, m.getContact().getId());
		TestCase.assertEquals(22, p.getOffice().getId());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("13980092699", o.getMobile().getNumber());
		TestCase.assertEquals("Library", p.getOffice().getBuilding());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("+86", o.getAdmin().getMobile().getZone());
	}
	
	//@Test
	public void testScanJAR() {
		var path = "d:/dragonfly.jar";
		var classes = Scanner.scanJar(path);
		TestCase.assertEquals(false, classes.isEmpty());
	}
}