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

package cn.techarts.dragonfly.data.dbutils;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;

import cn.techarts.dragonfly.data.DataException;
import cn.techarts.dragonfly.data.DataHelper;
import cn.techarts.dragonfly.data.SafeDataSource;
import cn.techarts.dragonfly.util.Hotpot;

/**
 * @author rocwon@gmail.com
 */
public class DbutilsExecutor implements DataHelper {
	private QueryRunner session;
	private Connection connection;
	private OrmBasedDbutils dbutils;
	
	@Override
	@SuppressWarnings("unchecked")
	public QueryRunner getExecutor() {
		return this.session;
	}
	
	@Override
	public Connection getConnection() {
		return this.connection;
	}
	
	public DbutilsExecutor(QueryRunner session, OrmBasedDbutils dbutils) {
		this.dbutils = dbutils;
		this.session = session;
		try {
			var ds = (SafeDataSource)session.getDataSource();
			this.connection = ds.getConnection();
			if(connection == null || connection.isClosed()) {
				throw new SQLException("Connection is null or closed.");
			}
			this.connection.setAutoCommit(true); //Default
		}catch(SQLException e) {
			throw new DataException("Failed to get connection.", e);
		}
	}
	
	@Override
	public int save(Object parameter, String... statement) throws DataException {
		return (int)dbutils.insert(Hotpot.getFirst(statement), parameter, true, session, connection);
	}

	@Override
	public int remove(Object parameter, String... statement) throws DataException {
		return this.dbutils.delete(Hotpot.getFirst(statement), parameter, session, connection);
	}

	@Override
	public int modify(Object parameter, String... statement) throws DataException {
		return this.dbutils.update(Hotpot.getFirst(statement), parameter, session, connection);
	}
	
	@Override
	public <T> T get(Object key, Class<T> clazz, String... statement) throws DataException {
		return this.dbutils.select(Hotpot.getFirst(statement), key, clazz, session, connection);
	}

	@Override
	public int getInt(Object parameter, String... statement) throws DataException {
		Integer result = dbutils.select(Hotpot.getFirst(statement), parameter, Integer.class, session, connection);
		return result != null ? result.intValue() : 0;
	}

	@Override
	public float getFloat(Object parameter, String... statement) throws DataException {
		Float result = dbutils.select(Hotpot.getFirst(statement), parameter, Float.class, session, connection);
		return result != null ? result.floatValue() : 0;
	}

	@Override
	public long getLong(Object parameter, String... statement) throws DataException {
		Long result = dbutils.select(Hotpot.getFirst(statement), parameter,Long.class, session, connection);
		return result != null ? result.longValue() : 0;
	}

	@Override
	public String getString(Object parameter, String... statement) throws DataException {
		return dbutils.select(Hotpot.getFirst(statement), parameter,String.class, session, connection);
	}	
	
	@Override
	public <T> List<T> getAll(Object parameter, Class<T> t, String... statement) throws DataException {
		return this.dbutils.selectAll(Hotpot.getFirst(statement), parameter, t, session, connection);
	}	
	
	@Override
	public <T> List<T> getAll( Class<T> t, String... statement) throws DataException {
		return this.dbutils.selectAll(Hotpot.getFirst(statement), null, t, session, connection);
	}	
	
	@Deprecated
	@Override
	public<T> List<T> get(Class<T> t, Object parameter, String... statement) throws DataException{
		return this.dbutils.selectAll(Hotpot.getFirst(statement), parameter, t, session, connection);
	}
	
	@Override
	public void close() throws DataException{
		try {
			getConnection().close();
		}catch(Exception e) {
			throw new DataException("Failed to close connection.", e);
		}
	}
}