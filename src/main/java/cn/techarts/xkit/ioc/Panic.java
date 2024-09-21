package cn.techarts.xkit.ioc;

/**
 * Exception of IoC
 */
public class Panic extends RuntimeException {
	
	private static final long serialVersionUID = -6501295436814757979L;
	
	public Panic(String cause) {
		super(cause);
	}
	
	public Panic(String cause, Throwable throwable) {
		super(cause, throwable);
	}
	
	public static Panic nullName() {
		return new Panic("The object name is null");
	}
	
	public static Panic nullContainer() {
		return new Panic("The object container is null!");
	}
	
	public static Panic cannotSetFieldValue(Exception e) {
		return new Panic("Failed to set field value.", e);
	}
	
	public static Panic classNotFound(String name, Throwable e) {
		return new Panic("Can't find the object with name [" + name + "]", e);
	}
	
	public static Panic classNotFound(String name) {
		return new Panic("Can't find the object with name [" + name + "]");
	}
	
	public static Panic cannotInstance(String name, Throwable e) {
		return new Panic("Failed to call the constructor of [" + name + "]", e);
	}
	
	public static Panic noQualifier(String name) {
		return new Panic("You must qualify the constructor parameter[" + name + "]");
	}
	
	public static Panic annotationConflicted() {
		return new Panic("Only one of Named or Valued annotation is allowed.");
	}
	
	public static Panic annotationMissing() {
		return new Panic("At least one of Named or Valued annotation is required.");
	}
	
	public static Panic noSingleton() {
		return new Panic("The object is not a singleton.");
	}
	
	public static Panic noDefaultConstructor(Class<?> arg, Throwable e) {
		return new Panic("Need a default constructor of class [" + arg.getName() + "]", e);
	}
	
	public static Panic noDefaultConstructor(Class<?> arg) {
		return new Panic("Need a default constructor of class [" + arg.getName() + "]");
	}
	
	public static Panic circularDependence(String name) {
		return new Panic("Circular dependence is not allowed: " + name);
	}
	
	public static Panic configKeyMissing(String key) {
		return new Panic("Can not find the key [" + key + "] in configuration.");
	}
	
	public static Panic typeConvertError(String type, String v, Exception e) {
		return new Panic("Can not convert [" + v + "] to type: " + type, e);
	}
	
	public static Panic unsupportedType(String name) {
		return new Panic("The data type [" + name + "] is unsupported.");
	}
	
	public static Panic failed2ParseJson(String file, Throwable e) {
		return new Panic("Failed to parse the json config: " + file, e);
	}
	
	public static Panic failed2ParseXml(String file, Throwable e) {
		return new Panic("Failed to parse the xml config: " + file, e);
	}
	
	public static Panic typeMissing(String arg) {
		return new Panic("The constructor parameter type is required: " + arg);
	}
}
