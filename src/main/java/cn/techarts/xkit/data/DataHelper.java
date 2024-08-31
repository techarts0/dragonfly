package cn.techarts.xkit.data;

import java.util.List;

public interface DataHelper extends AutoCloseable
{
	public static final String SQL_GETID = "getAutoId";
	
	public int save( final String statement, Object parameter, boolean... returnPK) throws DataException;
	
	public int remove( final String statement, Object parameter) throws DataException;
	
	public int modify( final String statement, Object parameter) throws DataException;
	
	/** Search an object by primary key(s). 
	 * @param Generally, it is a simple value such as an integer or string.
	 */
	public<T> T get( final String statement, Object key) throws DataException;
	
	public <T> T get(String statement, Object key, Class<T> clazz) throws DataException;
	
	public int getInt( final String statement, Object parameter) throws DataException;
	
	public float getFloat( final String statement, Object parameter) throws DataException;
	
	public long getLong( final String statement, Object parameter) throws DataException;
	
	public String getString( final String statement, Object parameter) throws DataException;
	
	public<T> List<T> getAll( final String statement, Object parameter)  throws DataException;
	
	public<T> List<T> getAll( final String statement, Object parameter, Class<T> t) throws DataException;
}