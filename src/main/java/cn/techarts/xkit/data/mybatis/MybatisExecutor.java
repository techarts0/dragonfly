/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.xkit.data.mybatis;

import java.util.List;
import java.sql.Connection;
import java.util.Collection;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import cn.techarts.xkit.app.UniObject;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.DataHelper;

/**
 * @author rocwon@gmail.com
 */
public class MybatisExecutor implements DataHelper {
	
	private SqlSession session = null;
	
	@Override
	@SuppressWarnings("unchecked")
	public SqlSession getExecutor() {
		return this.session;
	}
	
	@Override
	public Connection getConnection(){
		return session.getConnection();
	}
	
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
	public int save(Object parameter, String... statement) throws DataException {
		var sql = getStatement(statement);
		try{
			if(parameter == null) return 0;
			if(isEmptyCollection(parameter)) return 0;
			int rs = session.insert(sql, parameter);
			if(!(parameter instanceof UniObject))  return rs;
			var result = ((UniObject)parameter).getId();
			return this.retrievePrimaryKey(result, sql);
		}catch(Exception e){
			throw new DataException( "Failed to execute sql: [" + sql + "]", e);
		}
	}
	
	private int retrievePrimaryKey(int result, String statement) {
		if(result > 0) return result; //It's a correct primary key (AUTO-INCREASEMENT)
		throw new DataException(statement + ": Missing property [useGeneratedKeys]\n");
	}
	
	@Override
	public int remove(Object parameter, String... statement) throws DataException {
		var sql = getStatement(statement);
		try{
			if(isEmptyCollection(parameter)) return 0;
			return session.delete(sql, parameter);
		}catch(Exception e){
			throw new DataException( "Failed to execute sql: [" + sql + "]", e);
		}
	}

	@Override
	public int modify(Object parameter, String... statement) throws DataException {
		var sql = getStatement(statement);
		try{
			if(isEmptyCollection(parameter)) return 0;
			return session.update(sql, parameter);
		}catch(Exception e){
			throw new DataException( "Failed to execute sql: [" + sql + "]", e);
		}
	} 

	private<T> T get1(String statement, Object param) throws DataException {
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
	public <T> T get(Object key, Class<T> t, String... statement) throws DataException {
		return get1(getStatement(statement), key);
	}
	
	@Override
	public int getInt(Object parameter, String... statement) throws DataException{
		Object result = get1(getStatement(statement), parameter);
		if(result == null || !(result instanceof Integer)) return 0;
		return ((Integer)result).intValue();
	}
	
	@Override
	public float getFloat(Object parameter, String... statement) throws DataException{
		Object result = get1(getStatement(statement), parameter);
		if(result == null || !(result instanceof Float)) return 0;
		return ((Float)result).floatValue();
	}
	
	@Override
	public long getLong(Object parameter, String... statement) throws DataException{
		Object result = get1(getStatement(statement), parameter);
		if(result == null) return 0;
		if(result instanceof Long) return ((Long)result).longValue();
		if(result instanceof Integer) return ((Integer)result).longValue();
		return 0l;
	}
	
	@Override
	public String getString(Object parameter, String... statement) throws DataException{
		Object result = get1(getStatement(statement), parameter);
		return result != null && result instanceof String ? (String)result : null; //Latest reversion: returns an empty string("")
	}
	
	@Override
	public <T> List<T> getAll(Object parameter, Class<T> t, String... statement) throws DataException {
		return select(getStatement(statement), parameter, null);
	}
	
	@Override
	public <T> List<T> getAll(Class<T> t, String... statement) throws DataException {
		return select(getStatement(statement), null, null);
	}
	
	@Deprecated
	@Override
	public<T> List<T> get(Class<T> t, Object parameter, String... statement) throws DataException{
		return select(getStatement(statement), parameter, null);
	}
	
	private<T> List<T> select(String statement, Object parameter, RowBounds bounds) throws DataException{ 
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
	public void close() throws DataException{
		try {
			getConnection().close();
			this.session.close();
		}catch(Exception e) {
			throw new DataException("Failed to close connection.", e);
		}
	}
	
	private String getStatement(String[] statements) {
		if(statements == null) return null;
		if(statements.length == 0) return null;
		return statements[0]; //Note: maybe null here
	}
}