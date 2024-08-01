
package cn.techarts.xkit.data;

import org.springframework.dao.DataAccessException;

public class BasicDaoException extends DataAccessException
{	
	
	private static final long serialVersionUID = 1L;

	public BasicDaoException( String cause)
	{
		super( cause);
		
	}
	
	public BasicDaoException( String cause, Throwable e)
	{
		super( cause, e);
	}
	
	public BasicDaoException( int errno, String cause, Throwable e)
	{
		super( cause, e);
	}
}
