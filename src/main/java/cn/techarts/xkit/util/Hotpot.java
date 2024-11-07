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

package cn.techarts.xkit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Various kinds of UN-classifiable helper methods
 * @author rocwon@gmail.com
 */
public final class Hotpot {
	
	private static final Map<String, Integer> PRIMITIVES = new HashMap<>() {
		private static final long serialVersionUID = 1L;

	{
		put("java.lang.Integer",   1);    put("int",    1);
		put("java.lang.Long",      2);    put("long",   2);
		put("java.lang.Float",     3);    put("float",  3);
		put("java.lang.Short",     4);    put("short",  4);
		put("java.lang.Boolean",   5);    put("boolean",5);
		put("java.lang.Double",    6);    put("double", 6);
		put("java.lang.Byte",      7);    put("byte",   7);
		put("java.lang.Character", 8);    put("char",   8);
		put("java.lang.String",    9);    put("String", 9);
	}};
			
	
	public static boolean isPrimitive(Class<?> clazz) {
		var name = clazz.getName();
		return PRIMITIVES.containsKey(name);
	}
	
	private static String toFieldName(String method) {
		var chars = method.toCharArray();
		var idx = method.startsWith("is") ? 2 : 3;
		chars[idx] = (char)(chars[idx] + 32); //To lower-case
		return new String(slice(chars, idx, 100));
	}
	
	private static char[] slice(char[] arg, int start, int end) {
		if(arg == null || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new char[endIndex - start + 1];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	private static int getEndIndex(int end, int length) {
		return end < length ? end : length - 1;
	}
	
	private static boolean isGetter(String name) {
		if(name == null) return false;
		if(name.startsWith("is")) return true;
		return name.startsWith("get");
	}
	
	/**
	 * Map to Bean
	 */
	public static void fill(Object target, Map<String, Object> data) {
		if(target == null || data == null) return;
		try {
			var clazz = target.getClass();
			var methods = clazz.getMethods();
			if(methods == null) return;
			for(var m : methods) {
				var name = m.getName();
				if(!name.startsWith("set")) continue;
				if(m.getParameterCount() != 1) continue;
				var param = data.get(toFieldName(name));
				if(param != null) m.invoke(target, param);
			}
		}catch(Exception e) {
			throw new RuntimeException("Failed to fill values to the bean", e);
		}	
	}
	
	/**
	 * Bean to Map
	 */
	public static Map<String, Object> dump(Object target) {
		if(target == null) return Map.of();
		try {
			var clazz = target.getClass();
			var methods = clazz.getMethods();
			if(methods == null || methods.length == 0) return Map.of();
			var getters = new ArrayList<String>();
			for(var method : methods) {
				var name = method.getName();
				if(!isGetter(name)) continue;
				if(method.getParameterCount() > 0) continue;
				getters.add(name); //A legal getter method
			}
			if(getters.isEmpty()) return Map.of();
			var result = new HashMap<String, Object>(32);
			for(var getter : getters) {
				var m = clazz.getMethod(getter);
				if(m != null) {
					var f = toFieldName(getter);
					result.put(f, m.invoke(target));
				}
			}
			return result;
		}catch(Exception e) {
			throw new RuntimeException("Failed to dump the values to map", e);
		}
	}
	
	/**
	 * Supported Algorithm: AES
	 * @return Returns null if the key is invalid
	 */
	public static String decrypt(String target, byte[] key) {
		if(target == null || key == null) return null;
		try {
			var secretKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(toBytes(target)));
		}catch(Exception e) {
			throw new RuntimeException("Failed to decrypt [" + target + "]", e);
		}
	}
	
	/**
	 * Convert a hex string to bytes array
	 */
	public static byte[] toBytes(String hex) {
        if(hex == null) return null;
        var hexLength = hex.length();
        var chars = hex.toCharArray();
        var result = new byte[hexLength / 2];
        for(int i = 0; i < result.length; i++) {
        	var hc = String.valueOf(chars[i * 2]);
        	var lc = String.valueOf(chars[i * 2 + 1]);
        	var hi = Integer.parseInt(hc, 16);
        	var li = Integer.parseInt(lc, 16);
        	result[i] = (byte)(hi * 16 + li);
        }
        return result;
	}
	
	/**
	 * Supported Algorithm: AES
	 * @return Returns null if the key is invalid
	 */
	public static String encrypt(String source, byte[] key) {
		if(source == null || key == null) return null;
		try {
			var secretKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return toHex(cipher.doFinal(source.getBytes()), false);
		}catch(Exception e) {
			throw new RuntimeException("Failed to encrypt [" + source + "]", e);
		}
	}
	
	/**
	 *Convert bytes array to a hex string 
	 */
	public static String toHex(byte[] source, boolean upperCase) {
		var result = new StringBuilder(32);
		for(byte b : source) {
			int val = ((int)b) & 0xFF;
			if (val < 16) result.append("0");
			result.append(Integer.toHexString(val));
		}
		var encrypted = result.toString();
		return upperCase ? encrypted.toUpperCase() : encrypted;
    }
	
	public static String encrypt(String source, String algorithm){
		try{
			if(isNull(source)) return null;
			var mda = MessageDigest.getInstance(algorithm);
			byte[] original = source.getBytes("utf-8");
			mda.update(original);
			return toHex(mda.digest(original), false);
		}catch( Exception e){
			throw new RuntimeException( "Fail to encrypt [" + source + "].", e);
		}
	}
	
	public static Logger getLogger() {
		return Logger.getGlobal();
	}
	
	public static Logger getLogger(String name) {
		return Logger.getLogger(name);
	}
	
	/**
	 * Properties configuration
	 */
	public static Map<String, String> resolveProperties(String file) {
		var config = new Properties();
		var result = new HashMap<String, String>(64);
		try(var in = new FileInputStream(file)) {
			config.load(in);
			for(var key : config.stringPropertyNames()) {
				result.put(key, config.getProperty(key));
			}
			return result;
		}catch(IOException e) {
			throw new RuntimeException("Failed to load config [" + file + "]", e);
		}
	}	
	
	public static String getFirst(String[] statements) {
		if(statements == null) return null;
		if(statements.length == 0) return null;
		return statements[0]; //Note: maybe null here
	}
	
	private static final String P = "^\\s*[a-zA-Z\\s][a-zA-Z0-9_.-]*\\s*=\\s*.*$";
	
	/**
	 * INI-Liked configuration
	 */
	public static Map<String, String> resolveConfiguration1(String path){
		boolean multiLines = false;
		String line = null, sentence = null;
		var result = new HashMap<String, String>(512);
		try(var reader = new BufferedReader(new FileReader(path))){
			while((line = reader.readLine()) != null) {
				line = line.stripTrailing();
				if(line.isBlank()) continue;
				if(line.startsWith("#")) continue;
				if(multiLines && sentence != null) {
					var end = sentence.length() - 2;
					sentence = sentence.substring(0, end).concat(line);
				}
				if(!multiLines && line.matches(P)) sentence = line;
				multiLines = line.endsWith("\\\\"); //Current Line
				if(sentence == null || multiLines) continue;
				int i = sentence.indexOf('=');
				var k = sentence.substring(0, i);
				var v = sentence.substring(i + 1);
				if(v == null || v.length() == 0) continue;
				result.put(k.trim(), v.trim());
				sentence = null; //Reset the variable 
			}
			return result;
		}catch(IOException e){
			throw new RuntimeException("Fail to load the ini file", e);
		}
	}
	
	public static boolean orNull(Object arg0, Object arg1) {
		return arg0 == null || arg1 == null;
	}
	
	public static boolean isNull(String arg) {
		return arg == null || arg.isBlank();
	}
	
	public static boolean isNull(Object[] arg) {
		return arg == null || arg.length == 0;
	}
	
	public static boolean isNull(Map<?, ?> arg) {
		return arg == null || arg.isEmpty();
	}
	
	public static boolean isNull(List<?> arg) {
		return arg == null || arg.isEmpty();
	}
}

class ClassFilter implements FileFilter
{
	public boolean accept(File file){
		if(file == null) return false;
		if(file.isDirectory()) return true;
		var name =file.getName().toLowerCase();
		return name.endsWith(".class");
	}
}