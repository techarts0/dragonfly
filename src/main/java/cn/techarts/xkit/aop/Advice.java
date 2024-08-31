package cn.techarts.xkit.aop;

public interface Advice {
	
	/**
	 * @param args The parameters of target method you passed.
	 * @param result The return value of the target method.
	 * @param e An exception is threw if it's not NULL.
	 */
	public void execute(Object[] args, Object result, Throwable e);
}
