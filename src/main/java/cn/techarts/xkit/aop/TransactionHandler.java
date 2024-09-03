package cn.techarts.xkit.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.techarts.xkit.app.AbstractService;

public class TransactionHandler implements InvocationHandler {
	private Object target;
	
	public TransactionHandler(Object target) {
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		try{
			if(target instanceof AbstractService) {
				var as = (AbstractService)target;
				var dataHelper = as.getDataHelper();
			}
			result = method.invoke(this.target, args);
		}catch(Throwable e) {
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