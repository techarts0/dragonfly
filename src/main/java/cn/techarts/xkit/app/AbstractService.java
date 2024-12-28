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

import java.util.Objects;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;

import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.DataManager;
import cn.techarts.xkit.data.redis.RedisHelper;
import cn.techarts.xkit.data.trans.TransactionAbility;
import cn.techarts.xkit.data.trans.TransactionManager;
import cn.techarts.xkit.rpc.WebRpcHelper;

/**
 * Full RDB access and Redis caching capabilities are enabled.
 * 
 * @author rocwon@gmail.com
 */
public abstract class AbstractService implements TransactionAbility
{
	@Inject
	private DataManager dataManager = null;
	
	@Inject
	private RedisHelper redisHelper = null;
	
	@Inject
	private WebRpcHelper webRpcHelper = null;
		
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 */
	public static final int ERRID = 0;
	
	/**A tiny decimal that's very near to 0*/
	public static final double ZERO = 0.0000001D;
	
	@Override
	public TransactionManager getTransactionManager() {
		return this.dataManager;
	}
	
	/**
	 * Container manages transaction.
	 */
	protected DataHelper getDataHelper() {
		if(Objects.isNull(dataManager)) {
			throw new RuntimeException("Data module is not enabled.");
		}
		return dataManager.getExecutor();
	}
	
	protected RedisHelper getRedisHelper() {
		if(Objects.isNull(redisHelper) || !redisHelper.isInitialized()) {
			throw new RuntimeException("Cache module is not enabled.");
		}
		return this.redisHelper;
	}
	
	protected WebRpcHelper getRpcHelper() {
		if(Objects.isNull(webRpcHelper)) {
			throw new RuntimeException("RPC module is not enabled.");
		}
		return this.webRpcHelper;
	}
	
	/**
	 * Returns the MYBATIS Mapper object directly.
	 */
	protected<T> T getMapper(Class<T> mybatisMappClass) {
		var exec = getDataHelper().getExecutor();
		if(Objects.isNull(exec)) return null;
		if(!(exec instanceof SqlSession)) return null;
		return ((SqlSession)exec).getMapper(mybatisMappClass);
	}	
}