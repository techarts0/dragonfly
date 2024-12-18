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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cn.techarts.xkit.helper.Cryptor;

/**
 * @author rocwon@gmail.com
 */
public class SafeDataSource extends HikariDataSource {
	
	private static final String KEY = "b67fe6a8a28e2729c196deb99e6afd60";
	
	public SafeDataSource() {
		super();
	}
	
	public SafeDataSource(HikariConfig config) {
		super(config);
	}
	
	public static String decrypt(String password) {
		var key = Cryptor.toBytes(KEY);
		return Cryptor.decrypt(password, key);
	}
	
	@Override
	public void setPassword(String password) {
		super.setPassword(decrypt(password));
	}
	
	public void setDataSourcePassword(String password) {
		var val = decrypt(password);
		super.addDataSourceProperty("password", val);
	}
	
	@Override
	public void addDataSourceProperty(String propertyName, Object value){
		if("password".equals(propertyName)) {
			var val = decrypt((String)value);
			super.addDataSourceProperty("password", val);
		}else {
			super.addDataSourceProperty(propertyName, value);
		}
	}
	
	@Override
	public void setDataSourceProperties(Properties dsProperties){
		var pwd = dsProperties.getProperty("password");
		if(pwd != null) {
			dsProperties.setProperty("password", decrypt(pwd));
		}
		super.setDataSourceProperties(dsProperties);
	}
	
	public static String pwd(String encrypted, String token) {
		return "asdf!@#$".equals(token) ? decrypt(encrypted) : null;
	}
	
	public static String encrypt(String arg) {
		var key = Cryptor.toBytes(KEY);
		return Cryptor.encrypt(arg, key);
	}
	
	@Override
	public Connection getConnection() throws SQLException{
		return super.getConnection();
	}
}