package cn.techarts.xkit;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import cn.techarts.xkit.data.DataHelper;
import cn.techarts.xkit.data.RedisCacheHelper;

public abstract class AbstractService 
{
	@Autowired
	protected DataHelper database = null;
	
	@Autowired
	protected RedisCacheHelper cache = null;
	/**
	 * ERRID means the Id is ZERO(<b>0</b>) and it's <b>invalid</b>.
	 * In TECHARTS, ZERO(0) is reserved for system. 
	 * */
	public static final int ERRID = 0;
	public static final double ZERO = 0.00001D;
	
	public void setDatabase(DataHelper database) {
		this.database = database;
	}	
	
	public boolean checkId( int id)
	{
		return id < 1 ? false : true;
	}
	
	public boolean checkVarArg( int[] objects)
	{
		return objects != null && objects.length > 0 ? true : false; 
	}
	
	public static boolean empty( Collection<? extends Object> param)
	{
		return param == null || param.isEmpty();
	}
	
	public static boolean empty(IdObject obj) {
		return obj == null || obj.getId() == ERRID;
	}
	
	public static boolean empty(String src) {
		return src == null || src.trim().length() == 0;
	}
	
	public static boolean empty(Map<?, ?> param) {
		return param == null || param.isEmpty();
	}
	
	public<T> int get(Map<T,Integer> map, T key){
		Integer result = map.get( key);
		return result != null ? result : 0;
	}
	
	public String uuid(){
		return UUID.randomUUID().toString();
	}
	
	public static boolean yes(int arg) {
		return arg == 0 ? false : true;
	}
	
	public boolean yes(String arg) {
		if(arg == null) return false;
		if("".equals(arg)) return false;
		return "0".equals(arg) ? false : true;
	}
	
	/**
	 * Returns false if the parameter is NULL or owner equals 0
	 */
	protected <T extends UniqueObject> boolean validOwner(T arg){
		return arg == null || arg.getOwner() == ERRID ? false : true;
	}
	
	/**
	 * Returns false if the parameter is NULL or id equals 0
	 */
	protected <T extends IdObject> boolean yes(T arg) {
		return arg == null || arg.getId() == ERRID ? false : true;
	}
	
	public static final int SUCCESS = Results.Success.getId();
	public static final int FAILURE = Results.Failure.getId();	
}