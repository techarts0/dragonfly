/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.xkit.aop;

import java.lang.reflect.Proxy;

import cn.techarts.xkit.util.Hotpot;

/**
 * @author rocwon@gmail.com
 */
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
		if(Hotpot.isNull(interfaces))return target;
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
		if(Hotpot.isNull(interfaces))return target;
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
		if(Hotpot.isNull(interfaces))return target;
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
		if(Hotpot.isNull(interfaces))return target;
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
		if(Hotpot.isNull(interfaces))return target;
		return t.cast(Proxy.newProxyInstance(cl, interfaces, handler));
	}
}