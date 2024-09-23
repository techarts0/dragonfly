package cn.techarts.xkit.ioc;

import javax.inject.Provider;
import java.lang.reflect.*;

public class ProviderImpl<T> implements Provider<T> {
	
	private Class<T> clazz = null;
	private Craft dependence = null;
	
	public ProviderImpl(){}
	
	public ProviderImpl(Class<T> clazz, Craft dependence) {
		this.clazz = clazz;
		this.dependence = dependence;
	}
	
	public void setDependence(Craft dependence) {
		this.dependence = dependence;
	}
	
	@Override
	public T get() {
		if(dependence == null) return null;
		if(!dependence.isAssembled()) return null;
		return clazz.cast(dependence.instance().getInstance());
	}
}