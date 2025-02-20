package cn.techarts.dragonfly.data;

import java.util.Objects;
import java.util.logging.Logger;
import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Singleton;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cn.techarts.dragonfly.data.dbutils.DbutilsExecutor;
import cn.techarts.dragonfly.data.dbutils.QueryRunnerFactory;
import cn.techarts.dragonfly.data.mybatis.MybatisExecutor;
import cn.techarts.dragonfly.data.openjpa.JPASessionFactory;
import cn.techarts.dragonfly.data.openjpa.OpenJPAExecutor;
import cn.techarts.dragonfly.data.trans.Isolation;
import cn.techarts.dragonfly.data.trans.TransactionManager;
import cn.techarts.dragonfly.util.Hotpot;

@Singleton
public class DataManager extends JdbcSettings implements TransactionManager, AutoCloseable{
	
	private boolean initialized;
	
	//Apache MYBATIS
	private SqlSessionFactory mybatisFactory;
		
	//Apache DBUTILS
	private QueryRunnerFactory dbutilsFactory;
	
	//JPA(Default is Apache OPENJPA)
	private JPASessionFactory openJPAFactory;
		
	public static final String MYBATIS = "mybatis-config.xml";
	public static final String DBUTILS = "dbutils-config.xml";
	
	private ThreadLocal<DataHelper> threadLocal = new ThreadLocal<>();
	
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public DataManager() {}
	
	public DataHelper getExecutor() {
		var result = threadLocal.get();
		if(result != null) return result;
		result = getExecutor0();
		threadLocal.set(result);
		LOGGER.info("Obtained a connection wrapped in: " + result);
		return result; //Current Thread
	}
	
	private void initialize() {
		if(this.initialized) return; //CALL ONCE
		if("MYBATIS".equalsIgnoreCase(framework)){
			this.createMybatisSessionFactory();
		}else if("DBUTILS".equalsIgnoreCase(framework)){
			this.createDbutilsSessionFactory();
		}else if("OPENJPA".equals(framework)){ 
			this.createJpaEntityManagerFactory();
		}else{
			throw new DataException("Unsupported framework: " + framework);
		}
		this.initialized = true; //Ensure to Call Once
		LOGGER.info("Initialized the database factory with: " + framework);
	}
	
	/**
	 * Construct a JPA-based data helper;
	 */
	private void createJpaEntityManagerFactory() {
		if(this.openJPAFactory != null) return;
		try {
			this.openJPAFactory = new JPASessionFactory(driver, url, user, 
								       password, capacity, modelPackage);
		}catch(Exception e) {
			throw new DataException("Failed to initialize JPA EntityManager factory.", e);
		}
	}
	
	/**
	 * Construct a MYBATIS-based data helper
	 */
	private void createMybatisSessionFactory(){
		if(this.mybatisFactory != null) return;
		try {
			var config = Resources.getResourceAsStream(MYBATIS);
			this.mybatisFactory = new SqlSessionFactoryBuilder().build(config);
		}catch(IOException e) {
			throw new DataException("Failed to initialize mybatis session factory.", e);
		}
	}
	
	/**
	 * Construct an Apache DBUTILS-based data helper
	 */
	private void createDbutilsSessionFactory() {
		if(this.dbutilsFactory != null) return;
		try {
			var config = Resources.getResourceAsStream(DBUTILS);
			this.dbutilsFactory = new QueryRunnerFactory(config, driver, url, user, password, capacity);
		}catch(Exception e) {
			throw new DataException("Failed to initialize dbunits session factory.", e);
		}
	}
	
	private DataHelper getExecutor0() {
		this.initialize(); //CALL ONCE
		if(mybatisFactory != null) {
			var session = mybatisFactory.openSession(true);
			return new MybatisExecutor(session);
		}else if(dbutilsFactory != null) {
			var dbutils = dbutilsFactory.getDbutils();
			var session = dbutilsFactory.openQueryRunner();
			return new DbutilsExecutor(session, dbutils);
		}else if( this.openJPAFactory != null) {
			var session = openJPAFactory.getEntityManager();
			return new OpenJPAExecutor(session);
		}else {
			throw new DataException("Unsupported framework: " + framework);
		}
	}
	
	@Override
	public void close() {
		if(Objects.isNull(threadLocal)) return;
		this.threadLocal.remove();
		this.threadLocal = null;
		if(dbutilsFactory != null) {
			dbutilsFactory.close();
		}
		if(mybatisFactory != null) {
			mybatisFactory = null;
		}
		if(openJPAFactory != null) {
			openJPAFactory = null;
		}
		LOGGER.info("Closed the database factory.");
	}

	@Override
	public void begin(int level, boolean readonly) throws DataException {
		if(level == Isolation.NONE) return;
		var connection = getExecutor().getConnection();
		try {
			if(Objects.isNull(connection)) return;
			if(connection.isClosed()) return;
			connection.setAutoCommit(false);
			connection.setReadOnly(readonly);
			connection.setTransactionIsolation(level);
		}catch(SQLException e) {
			throw new DataException("Failed to begin a transaction", e);
		}
	}

	@Override
	public void rollback() throws DataException {
		var connection = getExecutor().getConnection();
		try {
			if(!connection.getAutoCommit()) {
				connection.rollback();
				connection.setAutoCommit(true);
			}
		}catch(SQLException e) {
			throw new DataException("Failed to rollback transaction.", e);
		}
	}
	
	/**
	 * 1. Commit transaction(if enabled).<br> 
	 * 2. Close the connection. <br>
	 * 3. Remove the connection from LocalThread.
	 */
	@Override
	public void commit() throws DataException {
		var executor = threadLocal.get();
		if(Objects.isNull(executor)) return; //Without
		var connection = executor.getConnection();
		try {
			if(connection.isClosed()) return;
			if(!connection.getAutoCommit()) {
				connection.commit();
				connection.setAutoCommit(true);
			}
			executor.close(); //Return the connection into pool
		}catch(Exception e) {
			throw new DataException("Failed to commit transaction.", e);
		}
		this.threadLocal.remove(); //It's very important
		LOGGER.info("Closed the connection wrapped in: " + executor);
	}
}