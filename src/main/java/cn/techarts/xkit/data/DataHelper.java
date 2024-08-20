/**
	Description: DataHelper.java
	Create date: 2007-10-20
	Update Date:
    Programmers: Pengfei Wang
	Version:  
*/

package cn.techarts.xkit.data;

import java.util.List;
import java.util.Map;

public interface DataHelper
{
	public static final String SQL_GETID = "getAutoId";
	
	public void close() throws BasicDaoException;
	
	public int save( final String statement, Object parameter, boolean... returnPK) throws BasicDaoException;
	
	public int remove( final String statement, Object parameter) throws BasicDaoException;
	
	public int update( final String statement, Object parameter) throws BasicDaoException;
	
	/** Search an object by primary key(s). 
	 * @param Generally, it is a simple value such as an integer or string.
	 */
	public<T> T get( final String statement, Object key) throws BasicDaoException;
	
	public Number getNumber( final String statement, Object parameter) throws BasicDaoException;
	
	public int getInt( final String statement, Object parameter) throws BasicDaoException;
	
	public float getFloat( final String statement, Object parameter) throws BasicDaoException;
	
	public long getLong( final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * The returning follows the C-LANG style(0:FALSE, NON-ZERO: TRUE)
	 */
	public boolean getBool( final String statement, Object parameter) throws BasicDaoException;
	
	public double getDouble( final String statement, Object parameter) throws BasicDaoException;
	
	public String getString( final String statement, Object parameter) throws BasicDaoException;
	
	public<T> List<T> getAll( final String statement, Object parameter)  throws BasicDaoException;
	
	public<K, V> Map<K, V> getAll( final String statement, Object parameter, String key)  throws BasicDaoException;
	
	public List<Integer> getIntegers(final String statement, Object parameter) throws BasicDaoException;
	
	public List<Float> getFloats(final String statement, Object parameter) throws BasicDaoException;
	
	public List<String> getStrings(final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * Returns TRUE if the given SQL exists in the statements map
	 */
	public boolean exists(String sql) throws BasicDaoException;
	
	public<T> List<T> query( final String statement,  Object parameter, int pageNumber, int pageSize) throws BasicDaoException;
	
	public<K, T> Map<K, T> query( final String statement, Object parameter, Class<T> t, int pageNumber, int pageSize, String key) throws BasicDaoException;
}