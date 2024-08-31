
package cn.techarts.xkit.data;

public class DataException extends RuntimeException
{	
	
	private static final long serialVersionUID = 1L;

	public DataException( String cause)
	{
		super( cause);
		
	}
	
	public DataException( String cause, Throwable e)
	{
		super( cause, e);
	}
	
	public DataException( int errno, String cause, Throwable e)
	{
		super( cause, e);
	}
}
