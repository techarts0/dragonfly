package cn.techarts.xkit.aop;

import java.lang.reflect.Proxy;

public class ProxyFactory {
	/**
	 * <b><i>IMPORTANT:</i></b><br> 
	 * 0. The generic type T must be an INTERFACE!<br>
	 * 1. The parameter target must implement the interface T.<br>
	 */
	public static<T> T create(T target, 
							  Advice[] beforeAdvices,
							  Advice[] afterAdvices,
							  Advice   throwAdvice,
							  Advice   finalAdvice,
							  Class<T> t) {
		if(t == null || !t.isInterface()) {
			throw AopException.notAnInterface(t);
		}
		var cl = target.getClass().getClassLoader();
		
		var handler = new DynamicHandler(target, 
										 beforeAdvices, 
										 afterAdvices, 
										 throwAdvice, 
										 finalAdvice);
		
		var interfaces = target.getClass().getInterfaces();
		return t.cast(Proxy.newProxyInstance(cl, interfaces, handler));
	}
	
	public static<T> T create(T target, 
							  Advice 	beforeAdvice,
							  Advice 	afterAdvice,
							  Advice   	throwAdvice,
							  Advice   	finalAdvice,
							  Class<T> t) {
		if(t == null || !t.isInterface()) {
		throw AopException.notAnInterface(t);
		}
		var cl = target.getClass().getClassLoader();
		
		var handler = new DynamicHandler(target, 
								 beforeAdvice, 
								 afterAdvice, 
								 throwAdvice, 
								 finalAdvice);
		
		var interfaces = target.getClass().getInterfaces();
		return t.cast(Proxy.newProxyInstance(cl, interfaces, handler));
	}
	
	public static<T> T create(T target, 
			  Advice 	beforeAdvice,
			  Advice 	afterAdvice,
			  Advice   	throwAdvice,		
			  Class<T> t) {
		if(t == null || !t.isInterface()) {
		throw AopException.notAnInterface(t);
		}
		var cl = target.getClass().getClassLoader();
		
		var handler = new DynamicHandler(target, 
						 beforeAdvice, 
						 afterAdvice, 
						 throwAdvice, 
						 null);
		
		var interfaces = target.getClass().getInterfaces();
		return t.cast(Proxy.newProxyInstance(cl, interfaces, handler));
	}
	
	public static<T> T create(T target, 
			  Advice 	beforeAdvice,
			  Advice 	afterAdvice,
			  Class<T> t) {
		if(t == null || !t.isInterface()) {
		throw AopException.notAnInterface(t);
		}
		var cl = target.getClass().getClassLoader();
		
		var handler = new DynamicHandler(target, 
						 beforeAdvice, 
						 afterAdvice, 
						 null, null);
		
		var interfaces = target.getClass().getInterfaces();
		return t.cast(Proxy.newProxyInstance(cl, interfaces, handler));
	}
	
	public static<T> T create(T target, 
			  Advice 	beforeAdvice,
			  Class<T> t) {
		if(t == null || !t.isInterface()) {
		throw AopException.notAnInterface(t);
		}
		var cl = target.getClass().getClassLoader();
		
		var handler = new DynamicHandler(target, 
						 beforeAdvice, 
						 null, null, null);
		
		var interfaces = target.getClass().getInterfaces();
		return t.cast(Proxy.newProxyInstance(cl, interfaces, handler));
	}
}