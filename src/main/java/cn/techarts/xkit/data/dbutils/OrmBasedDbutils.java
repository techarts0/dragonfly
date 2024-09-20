package cn.techarts.xkit.data.dbutils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.w3c.dom.Element;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.data.ParameterHelper;
import cn.techarts.xkit.util.Hotpot;

/**
 * A lightweight utility that's designed to access database.<br> 
 * It's based on APACHE DBUTILS and HIKARI connection pool.<p>
 * 
 * Persister supports named-parameter style: The placeholder in SQL is compatible to MyBatis 
 */
public class OrmBasedDbutils extends ParameterHelper{
	
	private Map<String, String> statements = null;
	
	public OrmBasedDbutils() {
		super();
		var xml = "/dbutils-config.xml";
		//var ini = "/dbutils-config.ini";
		statements = this.resolveConfiguration(xml);
		//statements = Hotpot.resolveConfiguration1(ini);
	}
	
	public String getStatement(String key) {
		var result = key == null ? null : "";
		if(key != null) {
			result = this.statements.get(key);
		}
		if(result != null) return result;
		throw new DataException("The sql [" + key + "] does not exist.");
	}
	
	/**
	 * Save the data into database table
	 * @return Returns the auto-increment key
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters
	 * @param returnKey true tells the method to return the auto-increment key 
	 */
	public long insert(String sql, Object params, boolean returnKey, QueryRunner session, Connection con){
		if(!returnKey) {
			return executeUpdateWithNamedParameters(sql, params, session, con);
		}else {
			return executeInsertWithNamedParameters(sql, params, session, con);
		}
	}
	
	/**
	 * Modify the data-rows in database table according to the given conditions
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 */	
	public int update(String sql, Object params, QueryRunner session, Connection con){
		return executeUpdateWithNamedParameters(sql, params, session, con);
	}
	
	/**
	 * Remove data-rows from database table according to the given conditions
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 * 
	 * */
	public int delete(String sql, Object params, QueryRunner session, Connection con){
		return executeUpdateWithNamedParameters(sql, params, session, con);
	}
	
	/**
	 * Execute batch of operations of INSERT, UPDATE or DELETE
	 */
	public int update(String sqlName, List<Object> params, QueryRunner session, Connection con) {
		var sql = getStatement(sqlName);
		var meta = this.parseStatement(sql, 0);
		int d0 = params.size(), d1 = meta.count();
		try {
			var args = new Object[d0][d1];
			for(int i = 0; i < d0; i++) {
				args[i] = meta.toParameters(params.get(i));
			}
			session.batch(con, meta.getSql(), args);
			return 0;			
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	/**
	 * Retrieve a data-row from database table and convert to your specified type according to the given conditions.
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 */
	public<T> T select(String sqlName, Object params, Class<T> clazz, QueryRunner session, Connection con){
		if(clazz == null) return null;
		var sql = getStatement(sqlName);
		var meta = parseStatement(sql, 0);
		if(meta == null || !meta.check()) {
			throw new DataException("Could not find the sql:" + sql);
		}
		ResultSetHandler<T> target = new BeanHandler<T>(clazz);
		if(Hotpot.isPrimitive(clazz)) {
			target = new ScalarHandler<T>(1); //A single value (primitive)
		}
		try {
			if(!meta.hasArgs()) {
				return session.query(con, meta.getSql(), target);
			}else {
				var args = meta.toParameters(params);
				return session.query(con, meta.getSql(), target, args);
			}			
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	/**
	 * Retrieve all data-rows what matched the given conditions and convert to your specified type
	 * @param sql The named SQL string
	 * @param params A java bean stores SQL parameters 
	 */
	public<T> List<T> selectAll(String sqlName, Object params, Class<T> clazz, QueryRunner session, Connection con){
		if(clazz == null) return null;
		var sql = getStatement(sqlName);
		var meta = parseStatement(sql, 0);
		ResultSetHandler<List<T>> target = new BeanListHandler<T>(clazz);
		if(Hotpot.isPrimitive(clazz)) {
			target = new ColumnListHandler<T>(1);
		}
		try {
			if(!meta.hasArgs()) {
				return session.query(con, meta.getSql(), target);
			}else {
				var args = meta.toParameters(params);
				return session.query(con, meta.getSql(), target, args);
			}	
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	/**
	 * Named Parameters
	 */
	private int executeUpdateWithNamedParameters(String sqlName, Object params, QueryRunner session, Connection con){
		var sql = getStatement(sqlName);
		var meta = parseStatement(sql, 0);
		try {
			if(!meta.hasArgs()) {
				return session.update(con, meta.getSql());
			}else {
				var args = meta.toParameters(params);
				return session.update(con, meta.getSql(), args);
			}
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	private long executeInsertWithNamedParameters(String sqlName, Object params, QueryRunner session, Connection con){
		Long result = null; //Auto-Increment ID
		var sql = getStatement(sqlName);
		var meta = parseStatement(sql, 0);
		var rsh = new ScalarHandler<Long>();
		try {
			if(!meta.hasArgs()) {
				result = session.insert(con, meta.getSql(), rsh);
			}else {
				var args = meta.toParameters(params);
				result = session.insert(con, meta.getSql(), rsh, args);
			}
			return result != null ? result.longValue() : 0L;
		}catch(SQLException e) {
			throw new DataException("Failed to execute the sql: " + sql, e);
		}
	}
	
	private Map<String, String> resolveConfiguration(String xml){
		try {
			var factory = DocumentBuilderFactory.newInstance();
			var stream = getClass().getResourceAsStream(xml);
            var doc = factory.newDocumentBuilder().parse(stream);
	        doc.getDocumentElement().normalize();
	        var sqls = doc.getElementsByTagName("sql");
	        var result = new HashMap<String, String>(256);
	        for(int i = 0; i < sqls.getLength(); i++) {
	        	var sql = sqls.item(i);
	        	var element = (Element)sql; //<sql>
	        	var name = element.getAttribute("id");
	        	var text = element.getTextContent();
	        	if(name != null && text != null) result.put(name, text);
	        }
	        return result;
        }catch(Exception e) {
        	throw new DataException("Failed to parse the file: " + xml, e);
        }
	}
}