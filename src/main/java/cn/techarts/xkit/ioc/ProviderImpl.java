package cn.techarts.xkit.ioc;

import javax.inject.Provider;

public class ProviderImpl<T> implements Provider<T> {
	
	private Craft bean = null;
	private Class<T> clazz = null;
	
	
	public ProviderImpl(){}
	
	public ProviderImpl(Class<T> clazz, Craft bean) {
		this.bean = bean;
		this.clazz = clazz;
	}
	
	@Override
	public T get() {
		if(bean == null) return null;
		if(!bean.isAssembled()) return null;
		return clazz.cast(bean.getInstance());
	}
}