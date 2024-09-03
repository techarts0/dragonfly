package cn.techarts.xkit.app;

import java.io.File;
import java.util.List;

import cn.techarts.xkit.aop.AopException;
import cn.techarts.xkit.aop.Bytecoder;
import cn.techarts.xkit.aop.Enhanced;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.trans.Transactional;
import cn.techarts.xkit.util.Helper;

public class ServiceEnhancer {
	private String classpath = null;
	public ServiceEnhancer(String classpath) {
		this.classpath = classpath;
	}
	
	public void start() {
		var cfs = this.scanClasses();
		var obj = AbstractService.class;
		if(cfs == null || cfs.isEmpty()) return;
		try {
			for(var cf : cfs) {
				var clzz = Class.forName(cf);
				if(!obj.isAssignableFrom(clzz)) continue;
				if(clzz.isAnnotationPresent(Enhanced.class)) continue;
				this.enhanceClassWithinTransaction(clzz);
			}
		}catch(Exception e) {
			throw new AopException("Failed to enhance services.", e);
		}
	}
	
	private List<String> scanClasses() {
		if(classpath == null || classpath.isBlank()) return null;
		var base = new File(classpath);//Root class-path
		if(base == null || !base.isDirectory()) return null;
		var start = base.getAbsolutePath().length();
		return Helper.scanClasses(base, start);
	}
	
	private void enhanceClassWithinTransaction(Class<?> service) {
		var methods = service.getDeclaredMethods();
		if(methods == null || methods.length == 0) return;
		var bytecoder = new Bytecoder(service);
		for(var method : methods) {
			if(!method.isAnnotationPresent(Transactional.class)) continue;
			bytecoder.beforeReturn(method.getName(), SRC_COMMIT);
			bytecoder.addCatch(method.getName(), SRC_ROLL, DATA_EX, "e");
		}
		bytecoder.save(true); //Save the enhanced class file  to recover the original
	}
	
	private static final String SRC_COMMIT = "super.commitAndClose();";
	private static final String SRC_ROLL = "getDataHelper().rollback(); throw e;";
	private static final Class<DataException> DATA_EX = DataException.class;
}
