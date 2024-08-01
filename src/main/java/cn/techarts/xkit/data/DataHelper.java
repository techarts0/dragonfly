/**
	Description: DataHelper.java
	Create date: 2007-10-20
	Update Date:
    Programmers: Pengfei Wang
	Version:  
*/

package cn.techarts.xkit.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public interface DataHelper
{
	public static final String SQL_GETID = "getAutoId";
	
	/** Get the original JDBC connection. You must close the connection after using.
	 *  @see Please refer to <H>releaseResources( Connection c, Statement s, ResultSet r)</H>.
	 * */
	public Connection getNativeJdbcConnection() throws BasicDaoException;
	
	/** An utility function helps us to close all JDBC resources */
	public void releaseResources( Connection c, Statement s, ResultSet r) throws BasicDaoException;
	
	/** Insert an object to database.
	 * */
	public int save( final String statement, Object parameter, boolean... returnPK) throws BasicDaoException;
	
	/** It's same to the method {@link save}<p>
	 * Insert an object into database and return a LONG key
	 * */
	public long create( final String statement, Object parameter, boolean... returnPK) throws BasicDaoException;
	
	/** Delete an object from database by some conditions. 
	 * */
	public int remove( final String statement, Object parameter) throws BasicDaoException;
	
	/** Modify an/some existing object(s) from database by some conditions.
	 * */
	public int update( final String statement, Object parameter) throws BasicDaoException;
	
	/** Search an object by its primary key(s). 
	 * @param Generally, this param is a simple value such as an integer or string.
	 *  @return The function returns an object which mapped in your XML file.
	 */
	public<T> T get( final String statement, Object key) throws BasicDaoException;
	
	public <T> T get(String statement, Object key, Class<T> t) throws BasicDaoException;
	
	/** Returns the INT column value according with the given conditions. The SQL statement looks like:
	 *  SELECT COUNT(*) FROM table WHERE conditions;
	 *  SELECT userId FROM table WHERE conditions; etc.	 
	 */
	public Number getNumber( final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * A specific case of the API Number getNumber returns an integer value
	 * */
	public int getInt( final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * A specific case of the API Number getNumber returns a float value
	 * */
	public float getFloat( final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * A specific case of the API Number getNumber returns a long value
	 * */
	public long getLong( final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * The returning follows the C-LANG style(0:FALSE, NON-ZERO: TRUE)
	 */
	public boolean getBool( final String statement, Object parameter) throws BasicDaoException;
	
	public double getDouble( final String statement, Object parameter) throws BasicDaoException;
	
	/** Returns the VARCHAR/CHAR column value according with the given conditions. The SQL statement looks like:
	 *  SELECT userName FROM table WHERE conditions;
	 *  SELECT description FROM table WHERE conditions; etc.	 
	 */
	public String getString( final String statement, Object parameter) throws BasicDaoException;
	
	/** Returns a set of objects which is according with the given conditions. 
	 *  @param <H>pageNumber</H>: Which page you want to get? If the argument is NULL or 0, 1 is default.
	 *  @param <H>pageSize</H>: How many rows you want per page? If the argument is NULL or 0, all rows will be returned.
	 * */
	public<T> List<T> query( final String statement,  Object parameter, Class<T> t, int pageNumber, int pageSize) throws BasicDaoException;
	
	public<T> List<T> query( final String statement,  Object parameter, int pageNumber, int pageSize) throws BasicDaoException;
	
	public<K, T> Map<K, T> query( final String statement, Object parameter, Class<T> t, int pageNumber, int pageSize, String key) throws BasicDaoException;
	
	/**
	 * Returns all of the result no paging. The method equals below:<br>
	 * query( statement, parameter, ClassOfT, 0, 0)
	 * */
	public<T> List<T> getAll( final String statement, Object parameter, Class<T> t)  throws BasicDaoException;
	
	public<T> List<T> getAll( final String statement, Object parameter)  throws BasicDaoException;
	
	/**
	 * The method requires the @param parameter which must NOT be null.<br>
	 * If it's null, a null object is returned.
	 */
	public<T> List<T> getAll2( final String statement, Object parameter)  throws BasicDaoException;
	
	public<T> Map<Integer, T> getAll( final String statement, Object parameter, Class<T> t, String key)  throws BasicDaoException;
	
	public List<Integer> getIntegers(final String statement, Object parameter) throws BasicDaoException;
	
	public List<Float> getFloats(final String statement, Object parameter) throws BasicDaoException;
	
	public List<String> getStrings(final String statement, Object parameter) throws BasicDaoException;
	
	/**
	 * Returns TRUE if the given SQL exists in the statements map
	 */
	public boolean exists(String sql) throws BasicDaoException;
}