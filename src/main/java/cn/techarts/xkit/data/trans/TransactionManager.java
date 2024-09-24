package cn.techarts.xkit.data.trans;

import cn.techarts.xkit.data.DataException;

public interface TransactionManager {
	/***
	 * Begin a transaction with specified attributes.
	 */
	public void begin(Isolation isolation, boolean readonly) throws DataException;
	
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
