package cn.techarts.xkit.data.dbutils;

import java.util.List;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.SafeDataSource;
import cn.techarts.xkit.util.Hotchpotch;

public class DbutilsExecutor implements DataHelper {
	private QueryRunner session;
	private OrmBasedDbutils dbutils;
	
	public DbutilsExecutor(QueryRunner session, OrmBasedDbutils dbutils) {
		this.dbutils = dbutils;
		this.session = session;
	}
	
	@Override
	public int save(Object parameter, String... statement) throws DataException {
		return (int)dbutils.insert(Hotchpotch.getFirst(statement), parameter, true, session);
	}

	@Override
	public int remove(Object parameter, String... statement) throws DataException {
		return this.dbutils.delete(Hotchpotch.getFirst(statement), parameter, session);
	}

	@Override
	public int modify(Object parameter, String... statement) throws DataException {
		return this.dbutils.update(Hotchpotch.getFirst(statement), parameter, session);
	}
	
	@Override
	public <T> T get(Object key, Class<T> clazz, String... statement) throws DataException {
		return this.dbutils.select(Hotchpotch.getFirst(statement), key, clazz, session);
	}

	@Override
	public int getInt(Object parameter, String... statement) throws DataException {
		Integer result = dbutils.select(Hotchpotch.getFirst(statement), parameter,Integer.class, session);
		return result != null ? result.intValue() : 0;
	}

	@Override
	public float getFloat(Object parameter, String... statement) throws DataException {
		Float result = dbutils.select(Hotchpotch.getFirst(statement), parameter,Float.class, session);
		return result != null ? result.floatValue() : 0;
	}

	@Override
	public long getLong(Object parameter, String... statement) throws DataException {
		Long result = dbutils.select(Hotchpotch.getFirst(statement), parameter,Long.class, session);
		return result != null ? result.longValue() : 0;
	}

	@Override
	public String getString(Object parameter, String... statement) throws DataException {
		return dbutils.select(Hotchpotch.getFirst(statement), parameter,String.class, session);
	}	
	
	@Override
	public <T> List<T> get(Class<T> t, Object parameter, String... statement) throws DataException {
		return this.dbutils.selectAll(Hotchpotch.getFirst(statement), parameter, t, session);
	}

	@Override
	public void rollback() {
		var ds = (SafeDataSource)session.getDataSource();
		if(ds == null) return;
		try {
			var connection = ds.getConnection();
			connection.rollback();
		}catch(SQLException e) {
			throw new DataException("Failed to rollback transaction.");
		}
	}

	@Override
	public void close() throws DataException {
		var ds = (SafeDataSource)session.getDataSource();
		if(ds == null) return;
		try {
			var connection = ds.getConnection();
			connection.commit();
			connection.close();
		}catch(SQLException e) {
			throw new DataException("Failed to commit transaction.");
		}
	}
}