package cn.techarts.xkit.app;

import java.util.Map;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.DatabaseFactory;
import cn.techarts.xkit.data.redis.RedisCacheHelper;
import cn.techarts.xkit.data.trans.Isolation;

public abstract class AbstractService 
{
	@Inject
	@Named
	private DatabaseFactory sqldb = null;
	
	@Inject
	@Named
	private RedisCacheHelper cache = null;
		
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 * */
	public static final int ERRID = 0;
	
	/**A tiny decimal that's very near to 0*/
	public static final double ZERO = 0.0000001D;
	
	/**
	 * Within a transaction.
	 */
	public DataHelper getDataHelper() {
		if(!sqldb.isInitialized()) {
			sqldb.initializeFactory();
		}
		return this.sqldb.getExecutor();
	}
	
	/**
	 * Begin a transaction(The service runs within a transaction)
	 */
	protected void beginTransaction(int level, boolean readonly) {
		var isolation = Isolation.to(level);
		getDataHelper().begin(isolation, readonly);
	}
	
	/**
	 * 1. Commit transaction(if enabled).<br> 
	 * 2. Close the connection. <br>
	 * 3. Remove the connection from LocalThread.
	 */
	protected void commitTransaction() {
		this.sqldb.closeExecutor();
	}
	
	protected void rollbackTransaction() {
		this.getDataHelper().rollback();
	}
	
	protected RedisCacheHelper cache() {
		if(cache == null || !cache.isInitialized()) {
			throw new RuntimeException("Redis is not enabled.");
		}
		return this.cache;
	}
	
	/**
	 * Returns true if the property id is greater than 0.
	 */
	protected boolean ok(int id)
	{
		return id < 1 ? false : true;
	}
	
	/**
	 * Returns true if the parameter objects is not null and contains at least 1 item.
	 */
	protected boolean ok( int[] objects)
	{
		return objects != null && objects.length > 0 ? true : false; 
	}
	
	/**
	 * Returns true if the parameter objects is not null and contains at least 1 item.
	 */
	protected<T extends Object> boolean ok( T[] objects)
	{
		return objects != null && objects.length > 0 ? true : false; 
	}
	
	/**
	 * Returns true if the parameter list is not null and contains at least 1 element.
	 */
	protected boolean ok(Collection<?> list) {
		return list != null && !list.isEmpty();
	}
	
	/**
	 * Returns true if the parameter map is not null and contains at least 1 element.
	 */
	protected boolean ok(Map<?, ?> map) {
		return map != null && !map.isEmpty();
	}
	
	/**
	 * Returns true if the string arg is not null or blank.
	 */
	protected boolean ok(String arg) {
		return arg != null && !arg.isBlank();
	}
	
	/**
	 * Returns false if the parameter is NULL or id equals 0
	 */
	protected <T extends UniObject> boolean ok(T arg) {
		return arg == null || arg.getId() == ERRID ? false : true;
	}
	
	protected<T extends UniObject> T error(T arg, int code, String text){
		if(arg == null) return null;
		arg.error(code, text);
		return arg;
	}
	
	/**
	 * Get the first item from the variable arguments.
	 */
	protected<T> T getFirst/**Of Variable Arguments*/(T[] objects) {
		if(objects == null) return null;
		if(objects.length == 0) return null;
		return objects[0]; //Note: maybe null here
	}
	
	/**
	 * The start index is inclusive and the end index is exclusive.
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] slice(T[] arg, int start, int end, Class<T> clazz) {
		if(!ok(arg) || end < start) return null;
		var length = getEndIndex(end, arg.length) - start;
		var result = Array.newInstance(clazz, length);
		System.arraycopy(arg, start, result, 0, length);
		return (T[])result; //Force to convert the generic type
	}
	
	/**
	 * The start index is inclusive and the end index is exclusive.
	 */
	public<T> List<T> slice(List<T> arg, int start, int end){
		if(!ok(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.size());
		return arg.subList(start, endIndex);
	}
	
	private int getEndIndex(int end, int length) {
		return end < length ? end : length - 1;
	}
}