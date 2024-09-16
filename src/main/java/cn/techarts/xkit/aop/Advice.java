package cn.techarts.xkit.aop;

public interface Advice {
	
	/**
	 * @param args The parameters of target method you passed.[All Advice]
	 * @param result The return value of the target method.[After Advice]
	 * @param e An exception is threw if it's not NULL. [Threw Advice]
	 */
	public void execute(Object[] args, Object result, Throwable e);
}
