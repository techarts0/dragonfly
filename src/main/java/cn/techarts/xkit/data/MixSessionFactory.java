package cn.techarts.xkit.data;

import java.io.IOException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import cn.techarts.xkit.data.dbutils.DbutilsExecutor;
import cn.techarts.xkit.data.dbutils.QueryRunnerFactory;
import cn.techarts.xkit.data.mybatis.MybatisExecutor;

public class MixSessionFactory {
	
	//MYBATIS
	private SqlSessionFactory factory1;
	
	//DBUTILS
	private QueryRunnerFactory factory2;
	
	/**
	 * Construct a MYBATIS-based data helper
	 */
	public MixSessionFactory(String configuration){
		try {
			var config = Resources.getResourceAsStream(configuration);
			this.factory1 = new SqlSessionFactoryBuilder().build(config);
		}catch(IOException e) {
			throw new DataException("Failed to initialize mybatis session factory.", e);
		}
	}
	
	/**
	 * Construct an Apache DBUTILS-based data helper
	 */
	public MixSessionFactory(String driver, String url, String user, String password, int max) {
		this.factory2 = new QueryRunnerFactory(driver, url, user, password, max);
	}
	
	public DataHelper getExecutor() {
		if(factory1 != null) {
			var session = factory1.openSession(false);
			return new MybatisExecutor(session);
		}else {
			var session = factory2.openQueryRunner();
			return new DbutilsExecutor(session, factory2.getDbutils());
		}
	}
	
	public DataHelper getExecutor(boolean enableTransaction) {
		if(factory1 != null) {
			var ac = !enableTransaction;
			var session = factory1.openSession(ac);
			return new MybatisExecutor(session);
		}else {
			var session = factory2.openQueryRunner();
			return new DbutilsExecutor(session, factory2.getDbutils());
		}
	}
}