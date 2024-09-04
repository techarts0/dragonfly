package cn.techarts.xkit.data;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cn.techarts.xkit.data.dbutils.DbutilsExecutor;
import cn.techarts.xkit.data.dbutils.QueryRunnerFactory;
import cn.techarts.xkit.data.mybatis.MybatisExecutor;
import cn.techarts.xkit.ioc.Valued;

public class DatabaseFactory implements AutoCloseable{
	@Inject
	@Valued(key="jdbc.url")
	private String url;
	
	@Inject
	@Valued(key="jdbc.username")
	private String user;
	
	@Inject
	@Valued(key="jdbc.driver")
	private String driver;
	
	@Inject
	@Valued(key="jdbc.password")
	private String password;
	
	@Inject
	@Valued(key="jdbc.capacity")
	private int capacity;
	
	@Inject
	@Valued(key="jdbc.framework")
	private String framework;
	
	private boolean initialized;
	
	//MYBATIS
	private SqlSessionFactory mybatisFactory;
		
	//DBUTILS
	private QueryRunnerFactory dbutilsFactory;
		
	public static final String MYBATIS = "mybatis-config.xml";
	
	private ThreadLocal<DataHelper> threadLocal = new ThreadLocal<>();
	
	public DatabaseFactory() {}
	
	/**
	 * Construct a MYBATIS-based data helper
	 */
	public void createMybatisSessionFactory(){
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
	public void createDbutilsSessionFactory() {
		if(this.dbutilsFactory != null) return;
		try {
			this.dbutilsFactory = new QueryRunnerFactory(driver, url, user, password, capacity);
		}catch(Exception e) {
			throw new DataException("Failed to initialize dbunits session factory.", e);
		}
	}
	
	public void initializeFactory() {
		if("MYBATIS".equalsIgnoreCase(framework)){
			this.createMybatisSessionFactory();
		}else if("DBUTILS".equalsIgnoreCase(framework)){
			this.createDbutilsSessionFactory();
		}else {
			throw new DataException("Unsupported orm framework: " + framework);
		}
		this.setInitialized(true); //
	}
	
	private DataHelper getExecutor0() {
		if(mybatisFactory != null) {
			var session = mybatisFactory.openSession();
			return new MybatisExecutor(session);
		}else if(dbutilsFactory != null) {
			var session = dbutilsFactory.openQueryRunner();
			return new DbutilsExecutor(session, dbutilsFactory.getDbutils());
		}else {
			throw new DataException("Unsupported orm framework: " + framework);
		}
	}
	
	public DataHelper getExecutor() {
		var result = threadLocal.get();
		if(result == null) {
			result = getExecutor0();
			this.threadLocal.set(result);
		}
		return result; //Current Thread
	}
	
	public void closeExecutor() {
		var current = threadLocal.get();
		if(current != null) {
			current.close();
			threadLocal.remove();
		}
	}
	
	public DataHelper getExecutor(boolean enableTransaction) {
		if(mybatisFactory != null) {
			var ac = !enableTransaction;
			var session = mybatisFactory.openSession(ac);
			return new MybatisExecutor(session);
		}else if(dbutilsFactory != null){
			var session = dbutilsFactory.openQueryRunner();
			return new DbutilsExecutor(session, dbutilsFactory.getDbutils());
		}else {
			throw new DataException("Unsupported orm framework: " + framework);
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
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
}