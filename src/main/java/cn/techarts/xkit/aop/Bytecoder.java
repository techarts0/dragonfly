package cn.techarts.xkit.aop;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

/**
 * Modify JAVA BYTECODE BASED ON THE LIBRARY JAVASSIST
 */
public final class Bytecoder {
	private ClassPool pool = null;
	private CtClass target = null;
	private String classpath = null;
	
	private static final String ENHANCED = "cn.techarts.xkit.aop.Enhanced";
	
	public Bytecoder(Class<?> clzz) {
		this(clzz.getName());
	}
	
	public Bytecoder(String source) {
		try {
			this.classpath = getPath(source);
			this.pool = ClassPool.getDefault();
			this.target = pool.get(source); 
		}catch(Exception e) {
			throw AopException.notFound(source, e);
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
	
	public void firstLine(String method, String code) {
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
        if (attr == null) {
        	attr = new AnnotationsAttribute(cp, at);
        }
        attr.addAnnotation(new Annotation(ENHANCED, cp));
		file.addAttribute(attr);
	}
}