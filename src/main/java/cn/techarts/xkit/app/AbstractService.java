/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.xkit.app;

import java.util.Map;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.DataManager;
import cn.techarts.xkit.data.redis.RedisHelper;
import cn.techarts.xkit.data.trans.TransactionManager;

/**
 * @author rocwon@gmail.com
 */
public abstract class AbstractService 
{
	@Inject
	private DataManager dataManager = null;
	
	@Inject
	private RedisHelper redisHelper = null;
		
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 */
	public static final int ERRID = 0;
	
	/**A tiny decimal that's very near to 0*/
	public static final double ZERO = 0.0000001D;
	
	protected TransactionManager getTransactionManager() {
		return this.dataManager;
	}
	
	/**
	 * Container manages transaction.
	 */
	protected DataHelper getDataHelper() {
		return dataManager.getExecutor();
	}
	
	protected RedisHelper getRedisHelper() {
		if(redisHelper == null || !redisHelper.isInitialized()) {
			throw new RuntimeException("Redis helper is not enabled.");
		}
		return this.redisHelper;
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
	protected <T> T[] slice(T[] arg, int start, int end, Class<T> clazz) {
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