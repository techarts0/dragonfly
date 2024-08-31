package cn.techarts.xkit.app;

import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.MixSessionFactory;
import cn.techarts.xkit.data.redis.RedisCacheHelper;

import javax.inject.Inject;
import javax.inject.Named;

import cn.techarts.xkit.data.DataException;

public abstract class AbstractService 
{
	@Inject
	@Named("mixSessionFactory")
	private MixSessionFactory sqldb = null;
	@Inject
	@Named("cacheHelper")
	private RedisCacheHelper cache = null;
	
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 * */
	public static final int ERRID = 0;
	public static final double ZERO = 0.00001D;
	
	public DataHelper getDataHelper() {
		return sqldb.getExecutor();
	}
	
	public void endService(DataHelper helper) {
		
	}
	
	public void setCache(RedisCacheHelper cache) {
		this.cache = cache;
	}
	
	public RedisCacheHelper cache() {
		if(cache == null || !cache.isInitialized()) {
			throw new DataException("cache is null.");
		}
		return this.cache;
	}
	
	/**
	 * Check the property id. (MUST be great than 0)
	 */
	public boolean ok( int id)
	{
		return id < 1 ? false : true;
	}
	
	/**
	 * Check the variable argument.(MUST be not null and at least 1 element)
	 */
	public boolean ok( int[] objects)
	{
		return objects != null && objects.length > 0 ? true : false; 
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
}