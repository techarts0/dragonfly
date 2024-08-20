package cn.techarts.xkit.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.DataAccessException;

import cn.techarts.xkit.UniObject;

public class MyBatisDataHelper implements DataHelper {

	private SqlSessionTemplate session = null;
	
	public MyBatisDataHelper(SqlSessionTemplate session) {
		this.session = session;
	}
	
	public void close() throws BasicDaoException{
		if(session != null) this.session.close();
	}
	
	private static boolean isEmptyCollection(Object parameter) {
		if(parameter == null) return false;
		if(!(parameter instanceof Collection)) return false;
		return ((Collection<?>)parameter).isEmpty();
	}
	
	/**
	 * IMPORTANT: A VERY DANGER BUG AND VERY EASY TO BE ACTIVATED IN CONCURRENT ENVIRONMENT<BR>
	 * WE CAN NOT GARANTEE THE MYSQL FUNCTION get_last_id() RETURNING A CORRECT ID (AUTO_INCREASEMENT COLUMN).<BR>
	 * SO, WE MUST USE JDBC getGeneratedKeys TO REPLACE IT. THE SOLUTION IS FOLLOWING:<BR>
	 * APPEND 2 PROPERTIES FOR YOU INSERT STATEMENT IN SQL MAPPER FILE LIKE THE FOLLOWING:<BR>
	 * <insert id="yourSqlName" parameterType="YourPoJo" useGeneratedKeys="true" keyProperty="id">
	 * 	
	 */
	@Override
	public int save(String statement, Object parameter, boolean... returnPK) throws BasicDaoException {
		try{
			if(parameter == null) return 0;
			if(!beforeCheck(statement)) return 0;
			if(isEmptyCollection(parameter)) return 0;
			this.session.insert(statement, parameter);
			if(!wantPrimaryKey(returnPK)) return 0;
			if(parameter instanceof UniObject) {
				var result = ((UniObject)parameter).getId();
				return this.retrievePrimaryKey(result, statement);
			}else {
				var result = session.selectOne(DataHelper.SQL_GETID);
				return result != null ? ((Integer)result).intValue() : 0;
			}
		}catch(DataAccessException e){
			throw new BasicDaoException( "Failed to insert the object. SQL: [" + statement + "]", e);
		}
	}
	
	private int retrievePrimaryKey(int result, String statement) {
		if(result > 0) return result; //It's a correct primary key (AUTO-INCREASEMENT)
		throw new BasicDaoException(statement + ": Missing property [useGeneratedKeys]\n");
	}
	
	@Override
	public int remove(String statement, Object parameter) throws BasicDaoException {
		try{
			if(isEmptyCollection(parameter)) return 0;
			return beforeCheck(statement) ? session.delete( statement, parameter) : 0;
		}catch( DataAccessException e){
			throw new BasicDaoException( "Failed to delete object [" + parameter + "]. SQL: [" + statement + "]", e);
		}
	}

	@Override
	public int update(String statement, Object parameter) throws BasicDaoException {
		try{
			if( !beforeCheck( statement)) return 0;
			if(isEmptyCollection(parameter)) return 0;
			return session.update( statement, parameter);
		}catch( DataAccessException e){
			throw new BasicDaoException( "Failed to update the object[" + parameter + "]. SQL: [" + statement + "]", e);
		}
	} 

	private<T> T get1( String statement, Object param) throws BasicDaoException 
	{
		if(param == null) return session.selectOne(statement);
		return session.selectOne(statement, param);
	}
	
	@Override
	public <T> T get(String statement, Object key) throws BasicDaoException {
		try{
			return get1( statement, key);
		}catch( DataAccessException e){
			throw new BasicDaoException( "Failed to search result by [" + key + "]. SQL: [" + statement + "]", e);
		}
	}
	
	@Override
	public Number getNumber( final String statement, Object parameter) throws BasicDaoException
	{
		Object result = get1( statement, parameter);
		return result != null && result instanceof Number ? (Number)result : 0;
	}
	
	@Override
	public int getInt( final String statement, Object parameter) throws BasicDaoException
	{
		Object result = get1( statement, parameter);
		if(result == null || !(result instanceof Integer)) return 0;
		return ((Integer)result).intValue();
	}
	
	@Override
	public float getFloat( final String statement, Object parameter) throws BasicDaoException
	{
		Object result = get1( statement, parameter);
		if(result == null || !(result instanceof Float)) return 0;
		return ((Float)result).floatValue();
	}
	
	@Override
	public long getLong( final String statement, Object parameter) throws BasicDaoException
	{
		Object result = get1( statement, parameter);
		if(result == null) return 0;
		if(result instanceof Long) return ((Long)result).longValue();
		if(result instanceof Integer) return ((Integer)result).longValue();
		return 0l;
	}
	
	@Override
	public double getDouble( final String statement, Object parameter) throws BasicDaoException
	{
		return getNumber(statement, parameter).doubleValue();
	}
	
	@Override
	public boolean getBool( final String statement, Object parameter) throws BasicDaoException
	{
		Object result = get1( statement, parameter);
		if(result == null) return false;
		if(!(result instanceof Integer)) return false;
		int value = ((Integer)result).intValue();
		return value > 0; //Follows the CLANG rule, 0 means FALSE and NON-ZERO is TRUE
	}
	
	@Override
	public String getString( final String statement, Object parameter) throws BasicDaoException
	{
		Object result = get1( statement, parameter);
		return result != null && result instanceof String ? (String)result : null; //Latest reversion: returns an empty string("")
	}
	
	@Override
	public<T> List<T> getAll( final String statement, Object parameter)  throws BasicDaoException
	{
		return select( statement, parameter, null);
	}
	
	@Override
	public<K, V> Map<K, V> getAll( final String statement, Object parameter, String key)  throws BasicDaoException
	{
		return select( statement, parameter, null, key);
	}
	
	@Override
	public List<Integer> getIntegers(final String statement, Object parameter) throws BasicDaoException
	{
		return select( statement, parameter, null);
	}
	
	@Override
	public List<Float> getFloats(final String statement, Object parameter) throws BasicDaoException
	{
		return select( statement, parameter, null);
	}
	
	@Override
	public List<String> getStrings(final String statement, Object parameter) throws BasicDaoException
	{
		return select( statement, parameter, null);
	}
	
	private boolean beforeCheck( final String statement)
	{
		if(session == null) {
			throw new BasicDaoException("SqlSessionTemplate is required!");
		}
		this.session.clearCache();
		return statement != null ? true : false;
	}
	
	private boolean wantPrimaryKey( boolean... args)
	{
		if(args == null) return false;
		return args.length >= 1 ? args[0] : false;
	}
	
	/**
	 * Returns TRUE if the given SQL exists in the statements map
	 */
	@Override
	public boolean exists(String sql) throws BasicDaoException{
		try {
			session.getConfiguration().getMappedStatement(sql, false);
			return true;
		}catch(IllegalArgumentException ex) {
			return false;
		}
	}	
	
	@Override
	public<K, T> Map<K, T> query( final String statement, Object parameter, Class<T> t, int pageNumber, int pageSize, String key) throws BasicDaoException
	{
		try{
			if( pageSize == 0 || pageNumber == 0) return select( statement, parameter, null, key);
			return select( statement, parameter, new RowBounds((pageNumber - 1) * pageSize, pageSize), key);
		}catch( DataAccessException e){
			throw new BasicDaoException( "Failed to search result for the argument [" + parameter + "]. SQL: [" + statement + "]", e);
		}
	}	
	
	@Override
	public<T> List<T> query( final String statement, Object parameter, int pageNumber, int pageSize) throws BasicDaoException
	{
		try{
			if( pageSize == 0 || pageNumber == 0) return select( statement, parameter, null);
			return select( statement, parameter, new RowBounds((pageNumber - 1) * pageSize, pageSize));
		}catch( DataAccessException e){
			throw new BasicDaoException( "Failed to search result for the argument [" + parameter + "]. SQL: [" + statement + "]", e);
		}
	}
	
	private<T> List<T> select( final String statement, Object parameter, RowBounds bounds) throws BasicDaoException
	{ 
		if(parameter == null) {
			return bounds == null ? session.selectList(statement) : session.selectList(statement, null, bounds);
		}else {
			return bounds == null ? session.selectList(statement, parameter) : session.selectList(statement, parameter, bounds);
		}
	}
	
	private<K, T> Map<K, T> select( final String statement, Object parameter, RowBounds bounds, String key) throws BasicDaoException
	{ 
		if(parameter == null) {
			return bounds == null ? session.selectMap(statement, key) : session.selectMap(statement, null, key, bounds);
		}else {
			return bounds == null ? session.selectMap(statement, parameter, key) : session.selectMap(statement, parameter, key, bounds);
		}
	}
}