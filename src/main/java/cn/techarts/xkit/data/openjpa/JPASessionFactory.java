package cn.techarts.xkit.data.openjpa;

import java.util.Map;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.apache.openjpa.persistence.PersistenceUnitInfoImpl;
import com.zaxxer.hikari.HikariConfig;
import cn.techarts.xkit.data.SafeDataSource;
import cn.techarts.xkit.ioc.Panic;
import cn.techarts.xkit.util.PackageScanner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

public class JPASessionFactory {
	private EntityManagerFactory factory;
	
	public JPASessionFactory(String driver, String url, String user, String password, int maxPoolSize, String pkg) {
		int capacity = maxPoolSize <= 0 ? 10 : maxPoolSize;
		var datasource = prepareDataSource(driver, url, user, password, capacity);
		var pui = new PersistenceUnitInfoImpl();
		pui.setNonJtaDataSource(datasource);
		pui.setPersistenceUnitName("OPENJPA");
		pui.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
		
		var base = this.getRootClassPath();
		var scanner = new PackageScanner(base, pkg);
		var managedClasses = scanner.scanJPAEntities();
		managedClasses.forEach(mc->pui.addManagedClassName(mc));
		
        var map = Map.of("openjpa.DataCache", "true",
        				 "openjpa.QueryCache", "true",
        				 "openjpa.TransactionMode", "false",
        		         "openjpa.ClassLoadEnhancement", "false",
        		         "openjpa.DynamicEnhancementAgent", "false",
        		         "openjpa.RuntimeUnenhancedClasses", "supported");
        var provider = new PersistenceProviderImpl();
        factory = provider.createContainerEntityManagerFactory(pui, map);
    }
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(result == null || result.getPath() == null) {
			throw new Panic("Failed to get resource path.");
		}
		return result.getPath();
	}
	
	
	public EntityManager getEntityManager() {
		return factory.createEntityManager();
	}
	
	private SafeDataSource prepareDataSource(String driver, String url, String user, String token, int maxPoolSize) {
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
		return new SafeDataSource(config);
	}
}
