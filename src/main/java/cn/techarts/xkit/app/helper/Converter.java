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

package cn.techarts.xkit.app.helper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Convert the specific type to another. For an incorrect input parameter,<p>
 * 1. returns 0 or 0.0 if the target type is a numeric, or<br>
 * 2. returns null if the target type is a date string. 
 * @author rocwon@gmail.com
 */
public final class Converter {
	public static int toInt( String arg){
		if(Empty.is(arg)) return 0;
		try{ 
			return Integer.parseInt(arg);
		}catch( NumberFormatException e){ 
			return 0;
		}
	}
	
	public static int toInt( String arg, boolean abs){
		if(Empty.is(arg)) return 0;
		try{ 
			int val = Integer.parseInt( arg);
			return abs && val < 0 ? Math.abs(val) : val;
		}catch( NumberFormatException e){ 
			return 0;
		}
	}
	
	public static int toInt( String arg, int defaultValue){
		if(Empty.is(arg)) return defaultValue;
		try{ 
			return Integer.parseInt(arg);
		}catch( NumberFormatException e){ 
			return defaultValue;
		}
	}
	
	public static double toDouble( String arg){
		if(Empty.is(arg)) return 0d;
		try{ 
			return Double.parseDouble(arg);
		}catch( NumberFormatException e){ 
			return 0d;
		}
	}
	
	public static double toDouble(byte[] bytes){
		if(Empty.is(bytes)) return 0d;
		var longBits = toLong(bytes);
		return Double.longBitsToDouble(longBits);
	}
	
	public static long toLong( String arg){
		if(Empty.is(arg)) return 0L;
		try{ 
			return Long.parseLong(arg);
		}catch(NumberFormatException e){ 
			return 0L;
		}
	}
	
	public static float toFloat(String arg){
		if(Empty.is(arg)) return 0f;
		try{ 
			return Float.parseFloat(arg);
		}catch( NumberFormatException e){ 
			return 0f;
		}
	}
	
	public static float toFloat(byte[] bytes){
		if(Empty.is(bytes)) return 0f;
		var intBits = toInt(bytes);
		return Float.intBitsToFloat(intBits);
	}
	
	/**
	 * A C-like style
	 */
	public static boolean toBoolean(int arg) {
		return arg != 0; //C-like style
	}
	
	/**
	 * @return Returns true if the parameter equals "1" or "true"
	 */
	public static boolean toBoolean(String arg) {
		if(Empty.is(arg)) return false;
		var val = arg.trim().toLowerCase();
		return val.equals("1") || val.equals("true");
	}
	
	public static String toString(int arg) {
		return String.valueOf(arg);
	}
	
	public static String toString(long arg) {
		return Long.toString(arg);
	}
	
	public static String toString(float arg) {
		return Float.toString(arg);
	}
	
	public static String toString(double arg) {
		return Double.toString(arg);
	}
	
	/**
	 * It's same to the method {@link DateHelper.format(arg, withTime)}<p>
	 * @param withTime Returns the long pattern "yyyy-MM-dd HH:mm:ss" if it's true. 
	 * Otherwise, the short pattern "yyyy-MM-dd" is returned. 
	 */
	public static String toString(Date arg, boolean withTime) {
		return Time.format(arg, withTime);
	}
	
	/**
	 *@param arg The pattern of date string is yyyy-MM-dd or yyyy-MM-dd HH:mm:ss<p>
	 *It's same to the method {@link DateHelper.parse(String arg)} 
	 */
	public static Date toDate(String arg) {
		return Time.parse(arg);
	}
	
	@SuppressWarnings("unchecked")
	public static<T> T[] toArray(Collection<T> arg) {
		if(arg == null) return null;
		return (T[])arg.toArray(); //Force to convert the generic type
	}
	
	public static<T> List<T> toList(T[] arg){
		if(arg == null) return null;
		var result = new ArrayList<T>();
		if(arg.length == 0) return result;
		for(var item : arg) {
			if(item != null) result.add(item);
		}
		return result;
	}
	
	public static<T> Set<T> toSet(T[] arg){
		if(arg == null) return null;
		if(arg.length == 0) {
			return new HashSet<>();
		}
		return new HashSet<>(toList(arg));
	}
	
	public static<T> T to(Object arg, Class<T> clazz) {
		if(arg == null || clazz == null) return null;
		try {
			return clazz.cast(arg);
		}catch(ClassCastException e) {
			return null; //Cannot cast to the given type
		}
	}
	
	//---------------------Bit Manipulation-----------------------------------
	
	public static int toInt(byte[] bytes) {
		if(bytes == null) return 0;
		if(bytes.length != 4) return 0;
		return  (bytes[3] & 0xFF) |
	            (bytes[2] & 0xFF) << 8 |
	            (bytes[1] & 0xFF) << 16 |
	            (bytes[0] & 0xFF) << 24;
	}
	
	public static int toIntLE(byte[] bytes) {
		if(bytes == null) return 0;
		if(bytes.length != 4) return 0;
		return  (bytes[0] & 0xFF)       |
	            (bytes[1] & 0xFF) << 8  |
	            (bytes[2] & 0xFF) << 16 |
	            (bytes[3] & 0xFF) << 24;
	}
	
	public static long toLong(byte[] bytes) {
		var result = 0L;
		if(bytes == null) return result;
		if(bytes.length != 8) return result;
		result <<= 8; 
		result |= (bytes[0] & 0xff);
		result <<= 8; 
		result |= (bytes[1] & 0xff);
		result <<= 8; 
		result |= (bytes[2] & 0xff);
		result <<= 8; 
		result |= (bytes[3] & 0xff);
		result <<= 8; 
		result |= (bytes[4] & 0xff);
		result <<= 8; 
		result |= (bytes[5] & 0xff);
		result <<= 8; 
		result |= (bytes[6] & 0xff);
		result <<= 8; 
		result |= (bytes[7] & 0xff);
		return result;
	}
	
	public static long toLongLE(byte[] bytes) {
		if(bytes == null) return 0L;
		if(bytes.length != 8) return 0L;
		return 	(bytes[0] & 0xFF)       |
		        (bytes[1] & 0xFF) << 8  |
		        (bytes[2] & 0xFF) << 16 |
		        (bytes[3] & 0xFF) << 24 |
		        (bytes[4] & 0xFF) << 32 |
	            (bytes[5] & 0xFF) << 40 |
	            (bytes[6] & 0xFF) << 48 |
	            (bytes[7] & 0xFF) << 56;
	}
	
	public static short toShort(byte[] bytes) {
		if(bytes == null) return 0;
		if(bytes.length != 2) return 0;
		var result = (bytes[1] & 0xFF) |
		         (bytes[0] & 0xFF) << 8;		        
		return (short)result;
	}
	
	public static short toShortLE(byte[] bytes) {
		if(bytes == null) return 0;
		if(bytes.length != 2) return 0;
		var result = (bytes[0] & 0xFF) |
		         (bytes[1] & 0xFF) << 8;		        
		return (short)result;
	}
	
	/**
	 * Right to left. For example:<p>
	 * 0x4b -> 01001011 ->{true, true, false, true, false, false, true, false} 
	 */
	public static boolean[] toBooleans(byte arg) {
		var result = new boolean[8];
		for(int i = 0; i < 8; i++) {
			var b = (arg >> i) & 0x01;
			result[i] = b == 1;
		}
		return result;	
	}
	
	public static byte[] toBytes(long val) {
		byte[] result = new byte[8];      
		result[0] = (byte)((val >> 56) & 0xff);
		result[1] = (byte)((val >> 48) & 0xff);
		result[2] = (byte)((val >> 40) & 0xff);
		result[3] = (byte)((val >> 32) & 0xff);
		result[4] = (byte)((val >> 24) & 0xff);
		result[5] = (byte)((val >> 16) & 0xff);
		result[6] = (byte)((val >> 8) & 0xff);
		result[7] = (byte)(val & 0xff);
		return result;
	}
	
	public static byte[] toBytesLE(long val) {
		var result = new byte[8];
		result[0] = (byte)val;
		result[1] = (byte)(val >> 8);
		result[2] = (byte)(val >> 16);
		result[3] = (byte)(val >> 24);
		result[4] = (byte)(val >> 32);
		result[5] = (byte)(val >> 40);
		result[6] = (byte)(val >> 48);
		result[7] = (byte)(val >> 56);
		return result;
	}
	
	public static byte[] toBytes(int val) {
		var result = new byte[4];
		result[0] = (byte)(val >> 24);
		result[1] = (byte)(val >> 16);
		result[2] = (byte)(val >> 8);
		result[3] = (byte)val;
		return result;
	}
	
	public static byte[] toBytesLE(int val) {
		var result = new byte[4];
		result[0] = (byte)val;
		result[1] = (byte)(val >> 8);
		result[2] = (byte)(val >> 16);
		result[3] = (byte)(val >> 24);
		return result;
	}
	
	public static byte[] toBytes(short val) {
		var result = new byte[2];
		result[0] = (byte)(val >> 8);
		result[1] = (byte)val;
		return result;
	}
	
	public static byte[] toBytesLE(short val) {
		var result = new byte[4];
		result[0] = (byte)val;
		result[1] = (byte)(val >> 8);
		return result;
	}
	
	public static byte[] hex2Bytes(String hex) {
		var str = "10" + hex; //For easier conversion
		var tmp = new BigInteger(str, 16).toByteArray();
	    return Slicer.slice(tmp, 1, tmp.length - 1);
	}
	
	public static String toHexString(String val) {
		if(Empty.is(val)) return null;
		var size = val.length() << 1;
		var result = new StringBuilder(size);
		var chars = val.toCharArray();
		for(var c : chars) {
			result.append(Integer.toHexString(c));
		}
		return result.toString().toUpperCase();
	}
}