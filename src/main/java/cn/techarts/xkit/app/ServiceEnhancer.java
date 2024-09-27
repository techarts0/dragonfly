package cn.techarts.xkit.app;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import cn.techarts.xkit.aop.AopException;
import cn.techarts.xkit.aop.Bytecoder;
import cn.techarts.xkit.aop.Enhanced;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.trans.Isolation;
import cn.techarts.xkit.data.trans.Transactional;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.Scanner;

/**
 * Provides transaction support for service.
 */
public class ServiceEnhancer {
	private String classpath = null;
	private static final Logger LOGGER = Hotpot.getLogger();
	
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
		return Scanner.scanClasses(base, start);
	}
	
	private void enhanceClassWithinTransaction(Class<?> service) {
		var methods = service.getDeclaredMethods();
		if(methods == null || methods.length == 0) return;
		var bytecoder = new Bytecoder(service);
		var t = Transactional.class; //Target
		for(var method : methods) {
			var trans = method.getAnnotation(t);
			if(trans == null) continue; //Without
			var level = trans.isolation();
			var readonly = trans.readonly();
			if(level != Isolation.NONE_TRANSACTION) {
				var src = BGNSRC(level.getLevel(), readonly);
				bytecoder.firstLine(method.getName(), src);
			}
			bytecoder.beforeReturn(method.getName(), SRC_COMMIT);
			bytecoder.addCatch(method.getName(), SRC_ROLL, DATA_EX, "e");
		}
		bytecoder.save(true); //Save the enhanced class file  to recover the original
		LOGGER.info("Enhanced the transaction service class: " + service.getName());
	}
	
	private static final String SRC_COMMIT = "getTransactionManager().commit();";
	private static final String SRC_ROLL = "getTransactionManager().rollback(); throw e;";
	private static final Class<DataException> DATA_EX = DataException.class;
	
	private static String BGNSRC(int level, boolean readonly) {
		var r = readonly ? "true" : "false";
		return "getTransactionManager().begin(" + level + ", " + r + ");";
	}
}
