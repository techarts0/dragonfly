package cn.techarts.xkit.data;

import java.util.Properties;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

public class SafeDataSourceFactory extends UnpooledDataSourceFactory {

	public SafeDataSourceFactory() {
	    this.dataSource = new SafeDataSource();
	}
	
	@Override
	public void setProperties(Properties properties) {
		var ds = (SafeDataSource)dataSource;
		
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
		
		//More properties...
	 }
}