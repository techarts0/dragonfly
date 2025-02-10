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

package cn.techarts.dragonfly.data.trans;

import cn.techarts.dragonfly.data.DataException;

/**
 * @author rocwon@gmail.com
 */
public interface TransactionManager {
	/***
	 * Begin a transaction with specified attributes.
	 * @param level Isolation Level. Please refer to {@link Isolation}
	 */
	public void begin(int level, boolean readonly) throws DataException;
	
	/**
	 * Roll-back the transaction if the DataException is threw.
	 */
	public void rollback() throws DataException;
	
	/**
	 * 1. Commit the transaction.<br>
	 * 2. Set the autoCommit to true.<br>
	 * 3. Close(return to pool) connection.<br>
	 */
	public void commit() throws DataException;
}
