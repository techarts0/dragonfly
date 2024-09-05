package cn.techarts.xkit.data.dbutils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.logging.log4j.Logger;

import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.util.Hotchpotch;

/**
 * A lightweight utility that's designed to access database.<br> 
 * It's based on APACHE DBUTILS and HIKARI connection pool.<p>
 * 
 * Persister supports named-parameter style: The placeholder in SQL is compatible to MyBatis 
 */
public class OrmBasedDbutils {
	
	private Map<Integer, SqlMeta> cachedStatements = null;
	private static final Logger LOGGER = Hotchpotch.getLogger(OrmBasedDbutils.class);
		
	public OrmBasedDbutils() {
		this.cachedStatements = new HashMap<>(512);
	}
	
	/**
	 * Save the data into database table
	 * @return Returns the auto-increment key
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters
	 * @param returnKey true tells the method to return the auto-increment key 
	 */
	public long insert(String sql, Object params, boolean returnKey, QueryRunner session){
		if(!returnKey) {
			return executeUpdateWithNamedParameters(sql, params, session);
		}else {
			return executeInsertWithNamedParameters(sql, params, session);
		}
	}
	
	/**
	 * Modify the data-rows in database table according to the given conditions
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 */	
	public int update(String sql, Object params, QueryRunner session){
		return executeUpdateWithNamedParameters(sql, params, session);
	}
	
	/**
	 * Remove data-rows from database table according to the given conditions
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 * 
	 * */
	public int delete(String sql, Object params, QueryRunner session){
		return executeUpdateWithNamedParameters(sql, params, session);
	}
	
	/**
	 * Execute batch of operations of INSERT, UPDATE or DELETE
	 */
	public int update(String sql, List<Object> params, QueryRunner session) {
		var meta = this.parseStatement(sql);
		int d0 = params.size(), d1 = meta.count();
		try {
			var args = new Object[d0][d1];
			for(int i = 0; i < d0; i++) {
				args[i] = meta.toParameters(params.get(i));
			}
			session.batch(meta.getSql(), args);
			return 0;			
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	/**
	 * Retrieve a data-row from database table and convert to your specified type according to the given conditions.
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 */
	public<T> T select(String sql, Object params, Class<T> clazz, QueryRunner session){
		if(clazz == null) return null;
		var meta = this.parseStatement(sql);
		if(meta == null || !meta.check()) {
			throw new DataException("Could not find the sql:" + sql);
		}
		ResultSetHandler<T> target = new BeanHandler<T>(clazz);
		if(Hotchpotch.isPrimitive(clazz)) {
			target = new ScalarHandler<T>(1); //A single value (primitive)
		}
		try {
			if(!meta.hasArgs()) {
				return session.query(meta.getSql(), target);
			}else {
				var args = meta.toParameters(params);
				return session.query(meta.getSql(), target, args);
			}			
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	/**
	 * Retrieve all data-rows what matched the given conditions and convert to your specified type
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 */
	public<T> List<T> selectAll(String sql, Object params, Class<T> clazz, QueryRunner session){
		if(clazz == null) return null;
		var meta = this.parseStatement(sql);
		ResultSetHandler<List<T>> target = new BeanListHandler<T>(clazz);
		if(Hotchpotch.isPrimitive(clazz)) {
			target = new ColumnListHandler<T>(1);
		}
		try {
			if(!meta.hasArgs()) {
				return session.query(meta.getSql(), target);
			}else {
				var args = meta.toParameters(params);
				return session.query(meta.getSql(), target, args);
			}	
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	/**
	 * Named Parameters
	 */
	private int executeUpdateWithNamedParameters(String sql, Object params, QueryRunner session){
		var meta = this.parseStatement(sql);
		try {
			if(!meta.hasArgs()) {
				return session.update(meta.getSql());
			}else {
				var args = meta.toParameters(params);
				return session.update(meta.getSql(), args);
			}
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	private long executeInsertWithNamedParameters(String sql, Object params, QueryRunner session){
		Long result = null; //Auto-Increment ID
		var meta = this.parseStatement(sql);
		var rsh = new ScalarHandler<Long>();
		try {
			if(!meta.hasArgs()) {
				result = session.insert(meta.getSql(), rsh);
			}else {
				var args = meta.toParameters(params);
				result = session.insert(meta.getSql(), rsh, args);
			}
			return result != null ? result.longValue() : 0L;
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	private SqlMeta parseStatement(String sql) {
		if(sql == null || sql.isBlank()) {
			throw new DataException("SQL is required!");
		}
		var key = Integer.valueOf(sql.hashCode());
		var result = this.cachedStatements.get(key);
		if(result == null) {
			result = parseNamedParameters(sql);
			if(result != null) cachedStatements.put(key, result);
		}
		if(result == null || !result.check()) {
			throw new DataException("Could not find the sql: " + sql);
		}
		LOGGER.info("Executing the SQL statement: " + result.getSql());
		return result;
	}
	
	private static SqlMeta parseNamedParameters(String sql) {
		if(sql == null || sql.isBlank()) return null;
		var chars = sql.toCharArray();
		var length = chars.length;
		var matched = false;
		var param = new StringBuilder(24);
		var stmt = new StringBuilder(256);
		var params = new ArrayList<String>();
		for(int i = 0; i < length; i++) {
			char ch = chars[i];
			if(matched) {
				if(ch != '}') {
					param.append(ch);
				}else { //End of the parameter
					matched = false;
					params.add(param.toString());
					param = new StringBuilder(24);
				}
			}else {
				stmt.append(ch);
				if(ch != '#') continue;
				if(chars[i++ + 1] != '{') continue;
				stmt.setCharAt(stmt.length() - 1, '?');
				matched = true; //Start of a parameter
			}
		}
		return new SqlMeta(stmt.toString(), params);
	}
	
	public static class SqlMeta{
		private String sql;
		private List<String> args;
		
		public SqlMeta(String statement, List<String> params) {
			this.setArgs(params);
			this.setSql(statement);
		}
		
		public boolean check() {
			return sql != null && !sql.isBlank();
		}
		
		/**
		 * The parameters number count
		 */
		public int count() {
			return args != null ? args.size() : 0;
		}
		
		public boolean hasArgs() {
			return args != null && !args.isEmpty();
		}	
		
		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}
		
		public List<String> getArgs() {
			return args;
		}

		public void setArgs(List<String> args) {
			this.args = args;
		}
		
		public Object[] toParameters(Object arg) {
			int count = this.count();
			if(arg == null || count == 0) return null;
			LOGGER.info("====> Parameters: " + arg);
			if(count == 1) {//1 parameter & Primitive Type
				if(arg instanceof Number) return new Object[] {arg};
				if(arg instanceof String) return new Object[] {arg};
				if(arg instanceof Boolean) return new Object[] {arg};
				if(arg instanceof Character) return new Object[] {arg};
			}
			var result = new Object[count];
			for(int i = 0; i < result.length; i++) {
				result[i] = getValue(arg, args.get(i));
			}
			return result;
		}
		
		private Object getValue(Object obj, String field) {
			if(obj == null || field == null) return null;
			var method = toMethodName("get", field);
			try {
				var raw = obj.getClass();
				var getter = raw.getMethod(method);
				if(getter == null) return null;
				return getter.invoke(obj);
			}catch(Exception e) {
				throw new DataException("Failed to get value.", e);
			}
		}
		
		private String toMethodName(String prefix, String field) {
			var chars = field.toCharArray();
			if (chars[0] >= 'a' && chars[0] <= 'z') {
				chars[0] = (char) (chars[0] - 32);
			}
			return prefix.concat(String.valueOf(chars));
		}
	}
}
