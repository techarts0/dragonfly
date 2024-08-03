package cn.techarts.xkit;

import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.RedisCacheHelper;
import cn.techarts.xkit.data.BasicDaoException;

public abstract class AbstractService 
{
	protected DataHelper persister = null;
	private RedisCacheHelper cache = null;
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 * */
	public static final int ERRID = 0;
	public static final double ZERO = 0.00001D;
	
	public void setPersister(DataHelper database) {
		this.persister = database;
	}
	
	public void setCache(RedisCacheHelper cache) {
		this.cache = cache;
	}
	
	public DataHelper persister() {
		if(persister == null) {
			throw new BasicDaoException("persister is null.");
		}
		return this.persister;
	}
	
	public RedisCacheHelper cache() {
		if(cache == null || !cache.isInitialized()) {
			throw new BasicDaoException("cache is null.");
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
}