package cn.techarts.xkit.data;

import java.util.logging.Logger;
import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Named;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import cn.techarts.xkit.data.dbutils.DbutilsExecutor;
import cn.techarts.xkit.data.dbutils.QueryRunnerFactory;
import cn.techarts.xkit.data.mybatis.MybatisExecutor;
import cn.techarts.xkit.data.openjpa.JPASessionFactory;
import cn.techarts.xkit.data.openjpa.OpenJPAExecutor;
import cn.techarts.xkit.data.trans.Isolation;
import cn.techarts.xkit.data.trans.TransactionManager;
import cn.techarts.xkit.util.Hotpot;

@Named
public class DataManager extends Settings implements TransactionManager, AutoCloseable{
	
	private boolean initialized;
	
	//MYBATIS
	private SqlSessionFactory mybatisFactory;
		
	//DBUTILS
	private QueryRunnerFactory dbutilsFactory;
	
	//JPA(Default is OPENJPA)
	private JPASessionFactory openJPAFactory;
		
	private static final String MYBATIS = "mybatis-config.xml";
	
	private ThreadLocal<DataHelper> threadLocal = new ThreadLocal<>();
	
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public DataManager() {}
	
	public DataHelper getExecutor() {
		this.initialize(); //If not...
		var result = threadLocal.get();
		if(result != null) return result;
		result = getExecutor0();
		this.threadLocal.set(result);
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
			this.dbutilsFactory = new QueryRunnerFactory(driver, url, user, password, capacity);
		}catch(Exception e) {
			throw new DataException("Failed to initialize dbunits session factory.", e);
		}
	}
	
	private DataHelper getExecutor0() {
		if(mybatisFactory != null) {
			var session = mybatisFactory.openSession(true);
			return new MybatisExecutor(session, threadLocal);
		}else if(dbutilsFactory != null) {
			var dbutils = dbutilsFactory.getDbutils();
			var session = dbutilsFactory.openQueryRunner();
			return new DbutilsExecutor(session, dbutils, threadLocal);
		}else if( this.openJPAFactory != null) {
			var session = openJPAFactory.getEntityManager();
			return new OpenJPAExecutor(session, threadLocal);
		}else {
			throw new DataException("Unsupported framework: " + framework);
		}
	}
	
	@Override
	public void close() {
		if(threadLocal == null) return;
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
			if(connection == null) return;
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
		var exec = getExecutor();
		var connection = exec.getConnection();
		try {
			if(!connection.getAutoCommit()) {
				connection.commit();
				connection.setAutoCommit(true);
			}
			exec.close(); //Return the connection into pool
		}catch(Exception e) {
			throw new DataException("Failed to commit transaction.", e);
		}
		//this.threadLocal.remove();
		LOGGER.info("Closed the connection wrapped in: " + exec);
	}
}