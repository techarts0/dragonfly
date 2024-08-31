package cn.techarts.xkit.data.dbutils;

import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.SafeDataSource;

public class DbutilsExecutor implements DataHelper {
	private QueryRunner session;
	private OrmBasedDbutils dbutils;
	
	public DbutilsExecutor(QueryRunner session, OrmBasedDbutils dbutils) {
		this.dbutils = dbutils;
		this.session = session;
	}
	
	@Override
	public void close() throws Exception {
		var ds = (SafeDataSource)session.getDataSource();
		if(ds != null) {
			var connection = ds.getConnection();
			connection.commit();
			connection.setAutoCommit(true);
		}
	}

	@Override
	public int save(String statement, Object parameter, boolean... returnPK) throws DataException {
		var rpk = returnPK != null && returnPK.length == 1 ? returnPK[0] : false; 
		return (int)this.dbutils.insert(statement, parameter, rpk, session);
	}

	@Override
	public int remove(String statement, Object parameter) throws DataException {
		return this.dbutils.delete(statement, parameter, session);
	}

	@Override
	public int modify(String statement, Object parameter) throws DataException {
		return this.dbutils.update(statement, parameter, session);
	}

	@Override
	public <T> T get(String statement, Object key) throws DataException {
		return null;
	}
	
	@Override
	public <T> T get(String statement, Object key, Class<T> clazz) throws DataException {
		return this.dbutils.select(statement, key, clazz, session);
	}

	@Override
	public int getInt(String statement, Object parameter) throws DataException {
		Integer result = dbutils.select(statement, parameter,Integer.class, session);
		return result != null ? result.intValue() : 0;
	}

	@Override
	public float getFloat(String statement, Object parameter) throws DataException {
		Float result = dbutils.select(statement, parameter,Float.class, session);
		return result != null ? result.floatValue() : 0;
	}

	@Override
	public long getLong(String statement, Object parameter) throws DataException {
		Long result = dbutils.select(statement, parameter,Long.class, session);
		return result != null ? result.longValue() : 0;
	}

	@Override
	public String getString(String statement, Object parameter) throws DataException {
		return dbutils.select(statement, parameter,String.class, session);
	}

	@Deprecated
	@Override
	public <T> List<T> getAll(String statement, Object parameter) throws DataException {
		return null;
	}
	
	@Override
	public <T> List<T> getAll(String statement, Object parameter, Class<T> t) throws DataException {
		return this.dbutils.selectAll(statement, parameter, t, session);
	}	
}