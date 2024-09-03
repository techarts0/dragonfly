package cn.techarts.xkit.aop;

public class AopException extends RuntimeException {
	
	private static final long serialVersionUID = -6501295436814757979L;

	public AopException(String cause) {
		super(cause);
	}
	
	public AopException(String cause, Throwable throwable) {
		super(cause, throwable);
	}
	
	public static AopException nullTarget() {
		return new AopException("The target object is null.");
	}
	
	public static AopException nullAdvice() {
		return new AopException("The advice object is null.");
	}
	
	public static AopException notAnInterface(Class<?> t) {
		return new AopException("The parameter t is not an interface: " + t.getName());
	}
	
	public static AopException notFound(String clzz, Throwable e) {
		return new AopException("Can not find the class or method: " + clzz, e);
	}
	
	public static AopException failedSaveFile(Throwable e) {
		return new AopException("Failed to save the class bytecode.", e);
	}
	
	
}