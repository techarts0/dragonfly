package cn.techarts.xkit.data;

import java.util.List;

public interface DataHelper
{
	public int save(Object parameter, String... statement) throws DataException;
	
	public int remove(Object parameter, String... statement) throws DataException;
	
	public int modify(Object parameter, String... statement) throws DataException;
	
	public <T> T get(Object key, Class<T> clazz, String... statement) throws DataException;
	
	public int getInt(Object parameter, String... statement) throws DataException;
	
	public float getFloat(Object parameter, String... statement) throws DataException;
	
	public long getLong(Object parameter, String... statement) throws DataException;
	
	public String getString(Object parameter, String... statement) throws DataException;
	
	public<T> List<T> getAll(Object parameter, Class<T> t, String... statement) throws DataException;
	
	
	//Start a transaction
	default void begin() throws DataException{}
	
	//Roll-back the transaction if failed
	public void rollback() throws DataException;
	
	//Clean up resources while shutdown
	public void close() throws DataException;
	
}