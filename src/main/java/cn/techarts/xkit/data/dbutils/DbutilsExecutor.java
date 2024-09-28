package cn.techarts.xkit.data.dbutils;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.SafeDataSource;
import cn.techarts.xkit.util.Hotpot;

public class DbutilsExecutor implements DataHelper {
	private QueryRunner session;
	private Connection connection;
	private OrmBasedDbutils dbutils;
	
	@Override
	@SuppressWarnings("unchecked")
	public QueryRunner getExecutor() {
		return this.session;
	}
	
	@Override
	public Connection getConnection() {
		return this.connection;
	}
	
	public DbutilsExecutor(QueryRunner session, OrmBasedDbutils dbutils) {
		this.dbutils = dbutils;
		this.session = session;
		try {
			var ds = (SafeDataSource)session.getDataSource();
			this.connection = ds.getConnection();
			if(connection == null || connection.isClosed()) {
				throw new SQLException("Connection is null or closed.");
			}
			this.connection.setAutoCommit(true); //Default
		}catch(SQLException e) {
			throw new DataException("Failed to get connection.", e);
		}
	}
	
	@Override
	public int save(Object parameter, String... statement) throws DataException {
		return (int)dbutils.insert(Hotpot.getFirst(statement), parameter, true, session, connection);
	}

	@Override
	public int remove(Object parameter, String... statement) throws DataException {
		return this.dbutils.delete(Hotpot.getFirst(statement), parameter, session, connection);
	}

	@Override
	public int modify(Object parameter, String... statement) throws DataException {
		return this.dbutils.update(Hotpot.getFirst(statement), parameter, session, connection);
	}
	
	@Override
	public <T> T get(Object key, Class<T> clazz, String... statement) throws DataException {
		return this.dbutils.select(Hotpot.getFirst(statement), key, clazz, session, connection);
	}

	@Override
	public int getInt(Object parameter, String... statement) throws DataException {
		Integer result = dbutils.select(Hotpot.getFirst(statement), parameter, Integer.class, session, connection);
		return result != null ? result.intValue() : 0;
	}

	@Override
	public float getFloat(Object parameter, String... statement) throws DataException {
		Float result = dbutils.select(Hotpot.getFirst(statement), parameter, Float.class, session, connection);
		return result != null ? result.floatValue() : 0;
	}

	@Override
	public long getLong(Object parameter, String... statement) throws DataException {
		Long result = dbutils.select(Hotpot.getFirst(statement), parameter,Long.class, session, connection);
		return result != null ? result.longValue() : 0;
	}

	@Override
	public String getString(Object parameter, String... statement) throws DataException {
		return dbutils.select(Hotpot.getFirst(statement), parameter,String.class, session, connection);
	}	
	
	@Override
	public <T> List<T> get(Class<T> t, Object parameter, String... statement) throws DataException {
		return this.dbutils.selectAll(Hotpot.getFirst(statement), parameter, t, session, connection);
	}	
	
	@Override
	public void close() throws DataException{
		try {
			getConnection().close();
		}catch(Exception e) {
			throw new DataException("Failed to close connection.", e);
		}
	}
}