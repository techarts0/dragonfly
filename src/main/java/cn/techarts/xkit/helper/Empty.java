package cn.techarts.xkit.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * As we know, many of bugs in java code are caused by null pointer,
 * the jhelper.Empty tries to reduce the NullPointerException errors.<p>
 * The concept of "Empty" means:<br>
 * 1. object is null<br>
 * 2. length of array is 0<br>
 * 3. size of collection is 0<br>
 * 4. length of string trims white space chars is 0<br>
 * 5. value of a numeric is 0, 0f, 0L, 0d
 */
public final class Empty {
	
	
	/**
	 * @return Returns true if the value of input parameter is 0, 0f, 0l or 0d 
	 */
	public static boolean zero(Number arg) {
		if(arg == null) return true;
		return arg.doubleValue() == 0;
	}
	
	public static boolean is(Object arg) {
		return arg == null;
	}
	
	/**
	 *@return If the parameter arg is null, returns the orElseOne. Otherwise the arg is returned directly
	 */
	public static Object is(Object arg, Object orElseOne) {
		return arg != null ? arg : orElseOne;
	}
	
	/**
	 * @return Returns true if any one in the array is null.<p>
	 * It equals the following code:<br>
	 * if(arg1 == null || arg2 == null || arg3 == null) {do something}
	 */
	public static boolean oneOf(Object... args) {
		if(args == null) return true;
		if(args.length == 0) return true;
		for(var arg : args) {
			if(arg == null) return true;
		}
		return false;
	}
	
	/**
	 *@return Returns true if all items in the array are null.<p>
	 *It equals the following code:<br> 
	 *if(arg1 == null && arg2 == null && arg3 == null) return false;
	 */
	public static boolean allOf(Object... args) {
		if(args == null) return true;
		if(args.length == 0) return true;
		for(var arg : args) {
			if(arg != null) return false;
		}
		return true;
	}
	
	/**
	 *@return Returns true if one of the items in the array is 0.<p>
	 *It equals the following code:<br>
	 *if(arg1 == 0 || arg2 == 0 || arg3 == 0) return false;
	 */
	public static boolean oneOfZero(int... args) {
		if(args == null) return true;
		if(args.length == 0) return true;
		for(var arg : args) {
			if(arg == 0) return true;
		}
		return false;
	}
	
	/**
	 *@return Returns true if one of the items in the array is 0.<p>
	 *It equals the following code:<br>
	 *if(arg1 == 0 && arg2 == 0 && arg3 == 0) return false;
	 */
	public static boolean allOfZero(int... args) {
		if(args == null) return true;
		if(args.length == 0) return true;
		for(var arg : args) {
			if(arg != 0) return false;
		}
		return true;
	}
	
	//----------------------------------------------------------------------------------------
	
	public static boolean is(String arg) {
		return arg == null || arg.trim().length() == 0;
	}
	
	public static boolean is(Object[] objs) {
		if(objs == null) return true;
		return objs.length == 0;
	}
	
	public static boolean is(int[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(long[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(float[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(double[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(boolean[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(byte[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(char[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(short[] args) {
		if(args == null) return true;
		return args.length == 0;
	}
	
	public static boolean is(Collection<?> arg) {
		return (arg == null || arg.isEmpty());
	}
	
	public static boolean is(Map<?, ?> arg) {
		return (arg == null || arg.isEmpty());
	}
	
	//----------------------------------------------------------------------------------------
	
	public static<T> List<T> list(){
		return new ArrayList<T>();
	}
	
	public static<T> Set<T> set(){
		return new HashSet<T>();
	}
	
	public static<K, V> Map<K, V> map(){
		return new HashMap<K, V>();
	}
	
	public static<T> List<T> immutableList(){
		return List.of();
	}
	
	public static<T> Set<T> immutableSet(){
		return Set.of();
	}
	
	public static<K, V> Map<K, V> immutableMap(){
		return Map.of();
	}
	
	//-------------------------------------------------------------------------------------------
	
	public static String trim(String arg) {
		return arg != null ? arg.trim() : null;
	}
}