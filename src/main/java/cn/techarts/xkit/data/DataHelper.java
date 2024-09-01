package cn.techarts.xkit.data;

import java.util.List;

public interface DataHelper extends AutoCloseable
{
	public int save(String statement, Object parameter) throws DataException;
	
	public int remove(String statement, Object parameter) throws DataException;
	
	public int modify(String statement, Object parameter) throws DataException;
	
	public <T> T get(String statement, Object key, Class<T> clazz) throws DataException;
	
	public int getInt(String statement, Object parameter) throws DataException;
	
	public float getFloat(String statement, Object parameter) throws DataException;
	
	public long getLong(String statement, Object parameter) throws DataException;
	
	public String getString(String statement, Object parameter) throws DataException;
	
	public<T> List<T> getAll(String statement, Object parameter, Class<T> t) throws DataException;
}