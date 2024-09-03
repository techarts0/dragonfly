package cn.techarts.xkit.data.mybatis;

import java.util.List;
import java.util.Collection;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import cn.techarts.xkit.app.UniObject;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.DataHelper;

public class MybatisExecutor implements DataHelper {
	
	private SqlSession session = null;
	
	public MybatisExecutor(SqlSession session) {
		this.session = session;
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
	public int save(String statement, Object parameter) throws DataException {
		try{
			if(parameter == null) return 0;
			if(isEmptyCollection(parameter)) return 0;
			int rs = session.insert(statement, parameter);
			if(!(parameter instanceof UniObject))  return rs;
			var result = ((UniObject)parameter).getId();
			return this.retrievePrimaryKey(result, statement);
		}catch(Exception e){
			throw new DataException( "Failed to execute sql: [" + statement + "]", e);
		}
	}
	
	private int retrievePrimaryKey(int result, String statement) {
		if(result > 0) return result; //It's a correct primary key (AUTO-INCREASEMENT)
		throw new DataException(statement + ": Missing property [useGeneratedKeys]\n");
	}
	
	@Override
	public int remove(String statement, Object parameter) throws DataException {
		try{
			if(isEmptyCollection(parameter)) return 0;
			return session.delete( statement, parameter);
		}catch(Exception e){
			throw new DataException( "Failed to execute sql: [" + statement + "]", e);
		}
	}

	@Override
	public int modify(String statement, Object parameter) throws DataException {
		try{
			if(isEmptyCollection(parameter)) return 0;
			return session.update( statement, parameter);
		}catch(Exception e){
			throw new DataException( "Failed to execute sql: [" + statement + "]", e);
		}
	} 

	private<T> T get1( String statement, Object param) throws DataException {
		try {
			if(param == null) {
				return session.selectOne(statement);
			}
			return session.selectOne(statement, param);
		}catch(Exception e) {
			throw new DataException( "Failed to execute sql: [" + statement + "]", e);
		}
	}
	
	@Override
	public <T> T get(String statement, Object key, Class<T> t) throws DataException {
		return get1( statement, key);
	}
	
	@Override
	public int getInt( final String statement, Object parameter) throws DataException
	{
		Object result = get1( statement, parameter);
		if(result == null || !(result instanceof Integer)) return 0;
		return ((Integer)result).intValue();
	}
	
	@Override
	public float getFloat( final String statement, Object parameter) throws DataException
	{
		Object result = get1( statement, parameter);
		if(result == null || !(result instanceof Float)) return 0;
		return ((Float)result).floatValue();
	}
	
	@Override
	public long getLong( final String statement, Object parameter) throws DataException
	{
		Object result = get1( statement, parameter);
		if(result == null) return 0;
		if(result instanceof Long) return ((Long)result).longValue();
		if(result instanceof Integer) return ((Integer)result).longValue();
		return 0l;
	}
	
	@Override
	public String getString( final String statement, Object parameter) throws DataException
	{
		Object result = get1( statement, parameter);
		return result != null && result instanceof String ? (String)result : null; //Latest reversion: returns an empty string("")
	}
	
	@Override
	public <T> List<T> getAll(String statement, Object parameter, Class<T> t) throws DataException {
		return select( statement, parameter, null);
	}
	
	private<T> List<T> select( final String statement, Object parameter, RowBounds bounds) throws DataException{ 
		try {
			if(parameter == null) {
				return bounds == null ? session.selectList(statement) : session.selectList(statement, null, bounds);
			}else {
				return bounds == null ? session.selectList(statement, parameter) : session.selectList(statement, parameter, bounds);
			}
		}catch(Exception e) {
			throw new DataException( "Failed to execute sql: [" + statement + "]", e);
		}
	}
	
	@Override
	public void rollback() throws DataException {
		if(session != null) session.rollback();
	}

	@Override
	public void close() throws DataException {
		if(session == null) return;
		this.session.close();
	}
}