package cn.techarts.xkit.ioc;

import javax.inject.Provider;

public class ProviderImpl<T> implements Provider<T> {
	
	private T object;
	
	public ProviderImpl() {
		
	}
	
	@Override
	public T get() {
		return null;
	}

}
