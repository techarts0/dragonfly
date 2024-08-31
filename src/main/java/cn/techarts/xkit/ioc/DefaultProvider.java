package cn.techarts.xkit.ioc;

import javax.inject.Provider;

public class DefaultProvider<T> implements Provider<T> {
	
	private String name;
	
	public DefaultProvider(String name) {
		this.name = name;
	}
	
	@Override
	public T get() {
		return null;
	}

}
