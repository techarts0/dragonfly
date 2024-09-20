package cn.techarts.xkit.data.dbutils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import com.zaxxer.hikari.HikariConfig;
import cn.techarts.xkit.data.SafeDataSource;
import cn.techarts.xkit.util.Hotpot;

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
	
	public QueryRunnerFactory(String driver, String url, String user, String password, int maxPoolSize) {
		this.ormdbutils = new OrmBasedDbutils();
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
