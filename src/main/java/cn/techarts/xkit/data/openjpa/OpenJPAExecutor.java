package cn.techarts.xkit.data.openjpa;

import java.util.List;
import cn.techarts.xkit.app.UniObject;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.ParameterHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class OpenJPAExecutor extends ParameterHelper implements DataHelper {
	
	private EntityManager session;
	
	public OpenJPAExecutor(EntityManager session) {
		this.session = session;
	}
	
	@Override
	public int save(Object parameter, String... statement) throws DataException {
		if(parameter == null) return 0;
		try {
			this.session.persist(parameter);
			if(!(parameter instanceof UniObject)) return 0;
			return ((UniObject)parameter).getId();
		}catch(Exception e) {
			throw new DataException("Failed to persist the object: " + parameter);
		}
	}

	@Override
	public int remove(Object parameter, String... statement) throws DataException {
		if(parameter == null) return 0;
		try {
			var jpql = this.getStatement(statement);
			if(jpql == null) {
				this.session.remove(parameter);
				return 1; //Effected Rows: 1
			}else {
				var meta = getSqlMeta(jpql);
				var clazz = parameter.getClass();
				var query = session.createQuery(jpql, clazz);
				setParameters(query, meta, parameter);
				return query.executeUpdate();
			}
		}catch(Exception e) {
			throw new DataException("Failed to remove the object: " + parameter);
		}
	}

	@Override
	public int modify(Object parameter, String... statement) throws DataException {
		if(parameter == null) return 0;
		try {
			var jpql = this.getStatement(statement);
			if(jpql == null) {
				this.session.merge(parameter);
				return 1; //Effected Rows: 1
			}else {
				var meta = getSqlMeta(jpql);
				var clazz = parameter.getClass();
				var query = session.createQuery(jpql, clazz);
				setParameters(query, meta, parameter);
				return query.executeUpdate();
			}
		}catch(Exception e) {
			throw new DataException("Failed to update the object: " + parameter);
		}
	}
	
	private <T> T get1(Object parameter, Class<T> clazz, String... statement) throws DataException {
		if(parameter == null || clazz == null) return null;
		try {
			var jpql = getStatement(statement);
			if(jpql == null) {
				return this.session.find(clazz, parameter);
			}else {
				var meta = getSqlMeta(jpql);
				var query = session.createQuery(jpql, clazz);
				this.setParameters(query, meta, parameter);
				return query.getSingleResult();
			}
		}catch(Exception e) {
			throw new DataException("Failed to find by the key: " + parameter, e);
		}
	}
	
	public <T> T get(Object key, Class<T> clazz, String... statement) throws DataException {
		return get1(key, clazz, statement);
	}
	
	@Override
	public int getInt(Object parameter, String... statement) throws DataException {
		return get(parameter, Integer.class, statement);
	}

	@Override
	public float getFloat(Object parameter, String... statement) throws DataException {
		return get(parameter, Float.class, statement);
	}

	@Override
	public long getLong(Object parameter, String... statement) throws DataException {
		return get(parameter, Long.class, statement);
	}

	@Override
	public String getString(Object parameter, String... statement) throws DataException {
		return get(parameter, String.class, statement);
	}

	@Override
	public <T> List<T> getAll(Object parameter, Class<T> t, String... statement) throws DataException {
		if(parameter == null || t == null) return null;
		var jpql = this.getStatement(statement);
		var meta = this.getSqlMeta(jpql);
		try {
			var query = this.session.createQuery(jpql, t);
			this.setParameters(query, meta, parameter);
			return query.getResultList();
		}catch(Exception e) {
			throw new DataException("Failed to execute the jpql: " + jpql, e);
		}
	}
	
	private<T> void setParameters(TypedQuery<T> query, SqlMeta meta, Object param) {
		if(!meta.hasArgs()) return;
		var args = meta.toParameters(param);
		for(int i = 0; i < args.length; i++) {
			query.setParameter(i, args[i]);
		}
	}
	
	private SqlMeta getSqlMeta(String jpql) {
		var result = this.parseStatement(jpql, 1);
		if(result == null || !result.check()) {
			throw new DataException("Could not find the jpql:" + jpql);
		}
		return result;
	}

	public void begin() throws DataException{
		session.getTransaction().begin();
	}
	
	@Override
	public void rollback() throws DataException {
		session.getTransaction().rollback();
	}

	@Override
	public void close() throws DataException {
		session.getTransaction().commit();
		session.close();
	}
}