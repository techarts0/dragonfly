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

package cn.techarts.xkit.app;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;

import cn.techarts.xkit.aop.AopException;
import cn.techarts.xkit.aop.Bytecoder;
import cn.techarts.xkit.aop.Enhanced;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.trans.Isolation;
import cn.techarts.xkit.data.trans.Transactional;
import cn.techarts.xkit.helper.Empty;
import cn.techarts.xkit.util.Hotpot;
import cn.techarts.xkit.util.Scanner;

/**
 * Provides transaction support for service.
 * @author rocwon@gmail.com
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
		if(Empty.is(cfs)) return;
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
	
	private boolean isPublic(Method m) {
		return Modifier.isPublic(m.getModifiers());
	}
	
	private void enhanceClassWithinTransaction(Class<?> service) {
		var methods = service.getDeclaredMethods();
		if(methods.length == 0) return;
		var bytecoder = new Bytecoder(service);
		var t = Transactional.class; //Target
		for(var method : methods) {
			if(!isPublic(method)) continue;
			var readonly = false;
			var trans = method.getAnnotation(t);
			var level = Isolation.NONE_TRANSACTION;
			if(trans != null) {
				level = trans.isolation();
				readonly = trans.readonly();
			}
			if(level != Isolation.NONE_TRANSACTION) {//BEGIN
				var src = BGNSRC(level.getLevel(), readonly);
				bytecoder.afterSignature(method.getName(), src);
			}
			
			bytecoder.beforeReturn(method.getName(), SRC_COMMIT); //ALWAYS
			
			if(level != Isolation.NONE_TRANSACTION) { //ROLLBACK
				bytecoder.addCatch(method.getName(), SRC_ROLL, DATA_EX, "e");
			}
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
