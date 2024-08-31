package cn.techarts.xkit.test;

import org.junit.Test;

import cn.techarts.xkit.aop.Advice;
import cn.techarts.xkit.aop.ProxyFactory;
import junit.framework.TestCase;

public class AopTest {
	
	@Test
	public void test() {
		var befores = new Advice[] {new BeforeAdvice(), new BeforeAdvice()};
		var afters = new Advice[] {new BeforeAdvice(), new BeforeAdvice()};
		
		var tmp = new TestServiceImpl();
		var service = ProxyFactory.create(tmp, befores, afters, null, null, TestService.class);
		var start = System.currentTimeMillis();
		
		
		for(int i = 0; i < 100000; i++) {
			var result = service.sayHello("Wu Xian");
		}
		System.out.println(System.currentTimeMillis() - start);
		
		
		
		//TestCase.assertEquals("Hello Wu Xian", result);
	}
}
