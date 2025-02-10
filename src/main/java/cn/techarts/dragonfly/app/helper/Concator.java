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

package cn.techarts.dragonfly.app.helper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 1. Combine 2 arrays, list or map as one,<br>
 * 2. or combine series strings as one.
 * @author rocwon@gmail.com
 */
public final class Concator {
	
	public static int[] concat(int[] array1, int[] array2) {
		var result = concat(array1, array2, int.class);
		return result != null ? (int[])result : new int[0];
	}
	
	public static float[] concat(float[] array1, float[] array2) {
		var result = concat(array1, array2, float.class);
		return result != null ? (float[])result : new float[0];
	}
	
	public static long[] concat(long[] array1, long[] array2) {
		var result = concat(array1, array2, long.class);
		return result != null ? (long[])result : new long[0];
	}
	
	public static double[] concat(double[] array1, double[] array2) {
		var result = concat(array1, array2, double.class);
		return result != null ? (double[])result : new double[0];
	}
	
	public static String[] concat(String[] array1, String[] array2) {
		var result = concat(array1, array2, String.class);
		return result != null ? (String[])result : new String[0];
	}
	
	public static Object[] concat(Object[] array1, Object[] array2) {
		var result = concat(array1, array2, Object.class);
		return result != null ? (Object[])result : new Object[0];
	}
	
	public static boolean[] concat(boolean[] array1, boolean[] array2) {
		var result = concat(array1, array2, boolean.class);
		return result != null ? (boolean[])result : new boolean[0];
	}
	
	public static byte[] concat(byte[] array1, byte[] array2) {
		var result = concat(array1, array2, byte.class);
		return result != null ? (byte[])result : new byte[0];
	}
	
	public static char[] concat(char[] array1, char[] array2) {
		var result = concat(array1, array2, char.class);
		return result != null ? (char[])result : new char[0];
	}
	
	/**
	 *@return Returns a string same to {@link String.concat} 
	 */
	public static String concat(String... args) {
		if(args == null || args.length == 0) return "";
		var result = new StringBuilder(64);
		for(var arg : args) {
			if(arg == null) continue;
			result.append(arg);
		}
		return result.toString();
	}
	
	
	 /**
	  * @return Returns a string that contains all elements in the INT array and separates with the given separator.<p>
	  * For example: [1, 2, 3, 4, 5] --> "1,2,3,4,5" (We suppose the separator is ',')
	  */
	 public static String concat(char separator, int... args) {
		 if(Empty.is(args)) return "";
		 var result = new StringBuilder(64);
		 for(var arg : args) {
			 result.append(separator);
			 result.append(arg);
		 }
		 if(result.length() == 0) return "";
		 return result.deleteCharAt(0).toString();
	 }
	 
	 /**
	  * @return Returns a string that contains all elements in the int array and separates with the given separator.<p>
	  * For example: ["ab", "cd", "ef", "g", "mn"] --> "ab,cd,ef,g,mn" (We suppose the separator is ',')
	  */
	 public static String concat(char separator, String... args) {
		 if(Empty.is(args)) return "";
		 var result = new StringBuilder(64);
		 for(var arg : args) {
			 if(Empty.is(arg)) continue;
			 result.append(separator);
			 result.append(arg);
		 }
		 if(result.length() == 0) return "";
		 return result.deleteCharAt(0).toString();
	 }
	 
	 /**
	  * @return Returns a string that concats all items in the list and separates with ','<p>
	  * For example: [1, 2, 3, 4, 5] --> "1,2,3,4,5".
	  */
	 public static String concat(List<Integer> args) {
		 return concat(args, ',');
	 }
	 
	 /**
	  * @return Returns a string that concats all items in the list and separates with the given separator.<p>
	  * For example: [1, 2, 3, 4, 5] --> "1,2,3,4,5".
	  */
	 public static String concat(List<Integer> args, char separator) {
		 if(Empty.is(args)) return "";
		 var result = new StringBuilder("");
		 for(var arg : args) {
		 	if(arg == null) continue;
		 	result.append(separator);
		 	result.append(arg);
		 }
		 return result.deleteCharAt(0).toString();
	}
	 
	 /**
	  * @return Returns a string that concats all items in the collection and separates with ','<p>
	  * For example: ["ab", "cd", "ef", "g", "mn"] --> "ab,cd,ef,g,mn".
	  */
	 public static String concat(Collection<String> args) {
		 return concat(args, ',');
	 }
	 
	 /**
	  * @return Returns a string that concats all items in the collection and separates with the given separator.<p>
	  * For example: ["ab", "cd", "ef", "g", "mn"] --> "ab,cd,ef,g,mn" (We suppose the separator is ',')
	  */
	 public static String concat(Collection<String> args, char separator) {
		 if(Empty.is(args)) return "";
		 var result = new StringBuilder(64);
		 for(var arg : args) {
		 	if(arg == null) continue;
		 	result.append(separator);
		 	result.append(arg);
		 }
		 return result.deleteCharAt(0).toString();
	}
		
	public static<T> List<T> concat(List<T> arg0, List<T> arg1){
		if(Empty.is(arg0)) return arg1;
		if(Empty.is(arg1)) return arg0;
		var result = new ArrayList<T>(arg0); 
		result.addAll(arg1);
		return result;
	}
	
	public static<T> Set<T> concat(Set<T> arg0, Set<T> arg1){
		if(Empty.is(arg0)) return arg1;
		if(Empty.is(arg1)) return arg0;
		var result = new HashSet<T>(arg0); 
		result.addAll(arg1);
		return result;
	}
	
	public static<K, V> Map<K, V> concat(Map<K, V> arg0, Map<K, V> arg1){
		if(Empty.is(arg0)) return arg1;
		if(Empty.is(arg1)) return arg0;
		var result = new HashMap<K, V>(arg0); 
		result.putAll(arg1);
		return result;
	}
	
	/**
	 * @return Returns a string like "%Good Morning%" using in SQL select statement. For example:<p>
	 * select * from user where name like '%Zhang san%'
	 */
	public static String queryString(String key) {
		if(Empty.is(key)) return null;
		return "%".concat(key).concat("%");
	}
	
	/**
	 * @param The element / component type in the array
	 */
	private static Object concat(Object array1, Object array2, Class<?> clazz) {
		if(array1 == null) return array2;
		if(array2 == null) return array1;
		int len1 = Array.getLength(array1);
		int len2 = Array.getLength(array2);
		int resultLength = len1 + len2;
		if(resultLength == 0) return null; //Without elements
		var result = Array.newInstance(clazz, resultLength);
		System.arraycopy(array1, 0, result, 0, len1);
		System.arraycopy(array2, 0, result, len1, len2);
		return result;
	}
}