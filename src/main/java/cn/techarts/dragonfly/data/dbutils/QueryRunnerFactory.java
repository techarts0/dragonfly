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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import com.zaxxer.hikari.HikariConfig;

import cn.techarts.dragonfly.data.SafeDataSource;
import cn.techarts.dragonfly.util.Hotpot;

/**
 * @author rocwon@gmail.com
 */
public class QueryRunnerFactory implements AutoCloseable {
	private SafeDataSource dataSource = null;
	private OrmBasedDbutils ormdbutils = null;	
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public OrmBasedDbutils getDbutils() {
		return this.ormdbutils;
	}
	
	/**
	 * @return Returns an instance of {@link QueryRunner} with  data-source
	 */
	public QueryRunner openQueryRunner(){
		if(dataSource == null) return null;
		return new QueryRunner(dataSource);
	}
	
	public QueryRunnerFactory(InputStream stream, String driver, String url, String user, String password, int maxPoolSize) {
		this.ormdbutils = new OrmBasedDbutils(stream);
		int poolsize = maxPoolSize <= 0 ? 10 : maxPoolSize;
		this.prepareDataSource(driver, url, user, password, poolsize);
		LOGGER.info("Connect to database with url: " + url);
	}
	
	@Override
	public void close() {
		if(dataSource == null) {
			this.dataSource.close();
		}
	}
	
	private void prepareDataSource(String driver, String url, String user, String token, int maxPoolSize) {
		var config = new HikariConfig();
		//Default: Transaction Enabled
		config.setAutoCommit(false);
		var pw = SafeDataSource.decrypt(token);
		if(driver.contains("Driver")) {
			config.setJdbcUrl(url);
			config.setUsername(user);
			config.setPassword(pw);
			config.setDriverClassName(driver);
		}else {
			config.setDataSourceClassName(driver);
			config.addDataSourceProperty("url", url);
			config.addDataSourceProperty("user", user);
			config.addDataSourceProperty("password", pw);
		}		
		config.setMaximumPoolSize(maxPoolSize);
		dataSource = new SafeDataSource(config);
	}
	
	/**
	 *@return Returns the pooled data source 
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	public Connection getConnection() {
		try {
			return this.dataSource.getConnection();
		}catch(SQLException e) {
			throw new RuntimeException("Failed to get connection from datasource.", e);
		}
	}
}
