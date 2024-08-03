package cn.techarts.xkit.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.techarts.xkit.Codec;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCacheHelper implements Closeable{
	
	private boolean initialized = false;
	private JedisPool connectionPool = null;
	
	public RedisCacheHelper(String host, int port, int poolSize) {
		if(host == null || port <= 0) return;
		int max = poolSize > 0 ? poolSize : 20;
		this.initConnectionPool(host, port, max);
	}
	
	private void initConnectionPool(String host, int port, int max) {
		var config = new JedisPoolConfig();
		config.setMinIdle(1);
		config.setMaxIdle(20);
		config.setMaxTotal(max);
		config.setTestOnBorrow(false);
		config.setTestOnReturn(false);
		config.setTestOnCreate(false);
		connectionPool = new JedisPool(config, host, port);
		this.initialized = true; //Initialized successfully.
	}
	
	public boolean isInitialized() {
		return this.initialized;
	}
	
	/**
	 * Uses the raw JEDIS API directly.
	 */
	public Jedis session() {
		if(connectionPool == null) return null;
		if(connectionPool.isClosed()) return null;
		return connectionPool.getResource();
	}

	@Override
	public void close() throws IOException {
		if(connectionPool == null) return;
		if(connectionPool.isClosed()) return;
		connectionPool.close();
		connectionPool.destroy();
	}
	
	public boolean save(int table, String key, Object value, int ttl) {
		if(key == null || value == null) return false;
		try(Jedis connection = session()){
			if(connection == null) return false;
			connection.select(table);
			if(value instanceof String) {
				connection.set(key, (String)value);
			}else {
				connection.set(key, serialize(value));
			}
			if(ttl > 0) connection.expire(key, ttl);
			return true;
		}
	}
	
	public void save(int table, String key, List<? extends Object> value, int ttl) {
		if(key == null || value == null) return;
		if(value.size() == 0) return; //At least ONE element
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			if(connection.exists(key)) connection.del(key);
			
			if(value.get(0) instanceof String) {
				connection.lpush(key, value.toArray(new String[]{}));
			}else {
				var tmp = new ArrayList<String>(64);
				value.forEach(val->tmp.add(serialize(val)));
				connection.lpush(key, tmp.toArray(new String[] {}));
			}
			if(ttl > 0) connection.expire(key, ttl);
		}
	}	
	
	/**
	 *Append an item into the specified list 
	 */
	public void append(int table, String listKey, Object value) {
		if(listKey == null || value == null) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			if(value instanceof String) {
				connection.rpush(listKey, (String)value);
			}else {
				connection.rpush(listKey, serialize(value));
			}
		}
	}	
	
	/**
	 * Save batch
	 * */
	public void save(int table, Map<String, ? extends Object> values) {
		if(values == null || values.isEmpty()) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			try(var pipeLine = connection.pipelined()){
				for(var val : values.entrySet()) {
					var tmp = val.getValue();
					if(tmp == null) continue;
					if(tmp instanceof String) {
						pipeLine.set(val.getKey(), (String)tmp);
					}else {
						pipeLine.set(val.getKey(), serialize(tmp));
					}
				}
				pipeLine.sync();
			}
		}
	}
	
	/**
	 * Save as a map
	 */
	@SuppressWarnings("unchecked")
	public void save(int table, String key, Map<String, ? extends Object> value, int ttl) {
		if(key == null || value == null || value.isEmpty()) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			var one = value.values().stream().findFirst();
			if(one.get() instanceof String) {
				connection.hset(key, (Map<String, String>)value);
			}else {
				var tmp = new HashMap<String, String>();
				value.entrySet().forEach(val->
				tmp.put(val.getKey(), serialize(val.getValue())));
			}
			if(ttl > 0) connection.expire(key, ttl);
		}
	}
	
	/**
	 * Append a key-value pair into the specified map
	 */
	public void append(int table, String key, String field, Object value) {
		if(key == null || field == null || value == null) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			if(value instanceof String) {
				connection.hset(key, field, (String)value);
			}else {
				connection.hset(key, field, serialize(value));
			}
		}
	}
	
	public void remove(int table, String key) {
		if(key == null) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			connection.del(key);
		}
	}
	
	public void remove(int table, String key, String field) {
		if(key == null || field == null) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			connection.hdel(key, field);
		}
	}
	
	/**Remove the item according to the specified index*/
	public void remove(int table, String key, int index) {
		if(key == null || index < 0) return;
		try(Jedis connection = session()){
			if(connection == null) return;
			connection.select(table);
			var val = connection.lindex(key, index);
			if(val != null) connection.lrem(key, 0, val);
		}
	}
	
	public String get(int table, String key) {
		if(key == null) return null;
		try(Jedis connection = session()){
			if(connection == null) return null;
			connection.select(table);
			return connection.get(key);
		}
	}
	
	public<T> T get(int table, String key, Class<T> t) {
		if(key == null) return null;
		try(Jedis connection = session()){
			if(connection == null) return null;
			connection.select(table);
			var result = connection.get(key);
			if(result == null) return null;
			return deserialize(result, t);
		}
	}
	
	public String getFromMap(int table, String key, String field){
		if(key == null) return null;
		try(Jedis connection = session()){
			if(connection == null) return null;
			connection.select(table);
			return connection.hget(key, field);
		}
	}
	
	public<T> T getFromMap(int table, String key, String field, Class<T> t){
		if(key == null) return null;
		try(Jedis connection = session()){
			if(connection == null) return null;
			connection.select(table);
			var result = connection.hget(key, field);
			if(result == null) return null;
			return deserialize(result, t);
		}
	}
	
	public String getFromList(int table, String listKey, int index){
		if(listKey == null) return null;
		try(Jedis connection = session()){
			if(connection == null) return null;
			connection.select(table);
			return connection.lindex(listKey, index);
		}
	}
	
	public<T> T getFromList(int table, String listKey, int index, Class<T> t){
		if(listKey == null) return null;
		try(Jedis connection = session()){
			if(connection == null) return null;
			connection.select(table);
			return deserialize(connection.lindex(listKey, index), t);
		}
	}
	
	public List<String> getList(int table, String key){
		if(key == null) return List.of();
		try(Jedis connection = session()){
			if(connection == null) return List.of();
			connection.select(table);
			return connection.lrange(key, 0, -1);
		}
	}
	
	public<T> List<T> getList(int table, String key, Class<T> t){
		if(key == null) return List.of();
		try(Jedis connection = session()){
			if(connection == null) return List.of();
			connection.select(table);
			var tmp = connection.lrange(key, 0, -1);
			if(tmp == null || tmp.isEmpty()) return List.of();
			var result = new ArrayList<T>(64);
			tmp.forEach(val->result.add(deserialize(val, t)));
			return result;
		}
	}
	
	public Map<String, String> getMap(int table, String key){
		if(key == null) return Map.of();
		try(Jedis connection = session()){
			if(connection == null) return Map.of();
			connection.select(table);
			var result = connection.hgetAll(key);
			return (result == null) ? Map.of() : result;
		}
	}
	
	public<T> Map<String, T> getMap(int table, String key, Class<T> t){
		if(key == null) return Map.of();
		try(Jedis connection = session()){
			if(connection == null) return Map.of();
			connection.select(table);
			var tmp = connection.hgetAll(key);
			if(tmp == null || tmp.isEmpty()) return Map.of();
			var result = new HashMap<String, T>(64);
			tmp.entrySet().forEach(e->
			result.put(e.getKey(), deserialize(e.getValue(), t)));
			return result;
		}
	}
		
	//-----------------Helper Methods----------------------------
	public static String serialize(Object object) {
		if(object == null) return null;
		var result = Codec.toCompactJson(object);
		return result;
	}
	
	/**
	 * The method returns an object according the given parameter <b>t</b>.<br>
	 * If the parameter <b>t</b> is illegal or an exception is threw, an empty object is returned.
	 * */
	public static<T> T deserialize(String json, Class<T> t) {
		if(json == null) return null;
		try {
			return Codec.decodeJson(json, t);
		}catch(Exception e) {
			return null;
		}
	}	
}