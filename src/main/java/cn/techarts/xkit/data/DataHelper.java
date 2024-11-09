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

package cn.techarts.xkit.data;

import java.util.List;
import java.sql.Connection;

/**
 * @author rocwon@gmail.com
 */
public interface DataHelper extends AutoCloseable{
	
	/**
	 * Returns the native executor what dependents on the under-lay framework.<p>
	 * MYBATIS: SqlSession<br>
	 * DBUTILS: QueryRunner<br>
	 * OPENJPA: EntityManager<p>
	 * 
	 * You have the full permission to manipulate the database on the connection.
	 */
	public<T> T getExecutor();
	
	/**
	 * Returns the native JDBC connection
	 */
	public Connection getConnection();
	
	@Override
	public void close() throws DataException;
	
	public int save(Object parameter, String... statement) throws DataException;
	
	public int remove(Object parameter, String... statement) throws DataException;
	
	public int modify(Object parameter, String... statement) throws DataException;
	
	public <T> T get(Object key, Class<T> clazz, String... statement) throws DataException;
	
	public int getInt(Object parameter, String... statement) throws DataException;
	
	public float getFloat(Object parameter, String... statement) throws DataException;
	
	public long getLong(Object parameter, String... statement) throws DataException;
	
	public String getString(Object parameter, String... statement) throws DataException;
	
	/**Please call the method getAll instead.*/
	@Deprecated
	public<T> List<T> get(Class<T> t, Object parameter, String... statement) throws DataException;
	
	/**Get all results which matched the specified conditions*/
	public<T> List<T> getAll(Object parameter, Class<T> t, String... statement) throws DataException;
	
	/**Get all results without conditions(like SELECT * FROM TABLE)*/
	public<T> List<T> getAll(Class<T> t, String... statement) throws DataException;
}