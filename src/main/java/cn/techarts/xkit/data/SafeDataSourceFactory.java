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

import java.util.Properties;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

import cn.techarts.xkit.util.Converter;

/**
 * @author rocwon@gmail.com
 */
public class SafeDataSourceFactory extends UnpooledDataSourceFactory {

	public SafeDataSourceFactory() {
	    this.dataSource = new SafeDataSource();
	}
	
	private int parseCapacity(String capacity) {
		if(capacity == null) return 10; //Default;
		try {
			return Converter.toInt(capacity);
		}catch(NumberFormatException e) {
			return 10;
		}
	}
	
	@Override
	public void setProperties(Properties properties) {
		var ds = (SafeDataSource)dataSource;
		var capacity = properties.getProperty("poolSize");
		ds.setMaximumPoolSize(parseCapacity(capacity));
		var driverClassName = properties.getProperty("jdbcDriver");
		if(driverClassName != null) {
			ds.setDriverClassName(driverClassName);
			ds.setJdbcUrl(properties.getProperty("jdbcUrl"));
			ds.setUsername(properties.getProperty("username"));
			ds.setPassword(properties.getProperty("password"));
		}
		 
		var dsClassName = properties.getProperty("dataSource");
		if(dsClassName != null) {
			ds.setDataSourceClassName(dsClassName);
			ds.addDataSourceProperty("url", properties.getProperty("url"));
			ds.addDataSourceProperty("user", properties.getProperty("user"));
			ds.addDataSourceProperty("password", properties.getProperty("password"));
		}
		
		//TODO More properties...
	 }
}
