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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author rocwon@gmail.com
 */
public class DynamicHandler implements InvocationHandler {
	private Object target;
	private Advice threw;		//Handle Exception
	private Advice fianlly;		//Clean Resources
	private Advice[] afters;	//Change Result
	private Advice[] befores;	//Initialize Context
	
	public DynamicHandler(Object target, Advice[] beforeAdvices, Advice[] afterAdvices, Advice finalAdvice, Advice throwAdvice) {
		if(Objects.isNull(target)) {
			throw AopException.nullTarget();
		}
		this.target = target;
		this.threw = throwAdvice;
		this.fianlly = finalAdvice;
		this.afters = afterAdvices;
		this.befores = beforeAdvices;
	}
	
	public DynamicHandler(Object target, Advice beforeAdvice, Advice afterAdvice, Advice finalAdvice, Advice throwAdvice) {
		this(target, new Advice[] {beforeAdvice}, new Advice[] {afterAdvice}, finalAdvice, throwAdvice);
	}
	
	public DynamicHandler(Object target, Advice beforeAdvice, Advice afterAdvice, Advice throwAdvice) {
		this(target, new Advice[] {beforeAdvice}, new Advice[] {afterAdvice}, null, throwAdvice);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		try{
			if(befores != null && befores.length > 0) {
				for(int i = 0; i < befores.length; i++) {
					var before = befores[i];
					if(before != null) {
						before.execute(args, null, null);
					}
				}
			}
			
			result = method.invoke(this.target, args);
			
			if(afters != null && afters.length > 0) {
				for(int i = 0; i < afters.length; i++) {
					var after = afters[i];
					if(after != null) {
						after.execute(args, result, null);
					}
				}
			}
		}catch(Throwable e) {
			if(threw != null) {
				threw.execute(args, null, getEx(e));
			}
		}finally {
			if(fianlly != null) {
				fianlly.execute(args, null, null);
			}
		}
		return result;
	}
	
	private Throwable getEx(Throwable e) throws Throwable {
		if(e instanceof InvocationTargetException) {
			return e.getCause(); //Business Exception
		}else if(e instanceof IllegalAccessException) {
			throw e; //It is not about your business.
		}else if(e instanceof IllegalArgumentException) {
			throw e; //It is not about your business.
		}else {
			return e; //The exception threw by target method
		}
	}
}