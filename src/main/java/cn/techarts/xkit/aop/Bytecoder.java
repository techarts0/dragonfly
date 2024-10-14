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

import java.util.Objects;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

/**
 * Modify JAVA BYTECODE BASED ON THE LIBRARY JAVASSIST
 * @author rocwon@gmail.com
 */
public final class Bytecoder {
	private ClassPool pool = null;
	private CtClass target = null;
	private String classpath = null;
	
	private static final String ENHANCED = "cn.techarts.xkit.aop.Enhanced";
	
	public Bytecoder(Class<?> clzz) {
		this(clzz.getName());
	}
	
	public Bytecoder(String clazz) {
		try {
			this.classpath = getPath(clazz);
			this.pool = ClassPool.getDefault();
			this.target = pool.get(clazz); 
		}catch(Exception e) {
			throw AopException.notFound(clazz, e);
		}
	}
	
	public void save(boolean setEnhancedTag) {
		try {
			if(setEnhancedTag) {
				setClassEnhancedTag();
			}
			target.writeFile(classpath);
		}catch(Exception e) {
			throw AopException.failedSaveFile(e);
		}
	}
	
	/**
	 * Returns the class location excluding packages.
	 */
	private String getPath(String source) throws ClassNotFoundException{
		var clzz = Class.forName(source);
		var domain = clzz.getProtectionDomain();
		var loc = domain.getCodeSource().getLocation();
		return loc.getPath();
	}
	
	public void beforeReturn(String method, String code) {
		try {
			var m = target.getDeclaredMethod(method);
			if(m != null) m.insertAfter(code);
		}catch(Exception e) {
			throw AopException.notFound(method, e);
		}
	}
	
	//The first line of the method
	public void afterSignature(String method, String code) {
		try {
			var m = target.getDeclaredMethod(method);
			if(m != null) m.insertBefore(code);
		}catch(Exception e) {
			throw AopException.notFound(method, e);
		}
	}
	
	public void addCatch(String method, String code, Class<? extends Throwable> ex, String name) {
		try {
			var exception = pool.get(ex.getName());
			var m = target.getDeclaredMethod(method);
			if(m != null) {
				m.addCatch(code, exception, name);
			}
		}catch(Exception e) {
			throw AopException.notFound(method, e);
		}
	}
	
	private void setClassEnhancedTag() {
		var file = target.getClassFile();
		var cp = file.getConstPool();
		var at = AnnotationsAttribute.visibleTag;
		var attr = (AnnotationsAttribute) file.getAttribute(at);
        if(Objects.isNull(attr)) {
        	attr = new AnnotationsAttribute(cp, at);
        }
        attr.addAnnotation(new Annotation(ENHANCED, cp));
		file.addAttribute(attr);
	}
}