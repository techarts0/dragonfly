package cn.techarts.xkit.data.openjpa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.apache.openjpa.persistence.PersistenceUnitInfoImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.zaxxer.hikari.HikariConfig;

import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.SafeDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

public class JPASessionFactory {
	private EntityManagerFactory factory;
	
	public JPASessionFactory(String driver, String url, String user, String password, int maxPoolSize) {
		int capacity = maxPoolSize <= 0 ? 10 : maxPoolSize;
		var datasource = prepareDataSource(driver, url, user, password, capacity);
		var pui = new PersistenceUnitInfoImpl();
		pui.setNonJtaDataSource(datasource);
		pui.setPersistenceUnitName("OPENJPA");
		pui.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
		
		var managedClasses = this.parseMappingClasses();
		managedClasses.forEach(mc->pui.addManagedClassName(mc));
		
        var map = Map.of("openjpa.DataCache", "true",
        				 "openjpa.QueryCache", "true",
        		         "openjpa.ClassLoadEnhancement", "false",
        		         "openjpa.DynamicEnhancementAgent", "false",
        		         "openjpa.RuntimeUnenhancedClasses", "supported");
        
        var provider = new PersistenceProviderImpl();
        this.factory = provider.createContainerEntityManagerFactory(pui, map);
	}
	
	private List<String> parseMappingClasses(){
		var path = getClass().getResource("/");
		var base = path.getPath() + "jpa-mapping.json";
		var parser = new ObjectMapper();
		try {
			var file = new File(base);
			var nodes = parser.readValue(file, ArrayNode.class);
			if(nodes == null || nodes.isEmpty()) return List.of();
			var result = new ArrayList<String>();
			for(var node : nodes) {
				result.add(node.toString());
			}
			return result;
		}catch(Exception e) {
			throw new DataException("Failed to parse JPA mapping file.", e);
		}
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
