package cn.techarts.xkit.util;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.techarts.xkit.ioc.Panic;

/**
 * Various kinds of UN-classifiable helper methods
 */
public final class Hotchpotch {
	public static List<String> scanClasses(File dest, int start){
		var result = new ArrayList<String>();
		var tmp = dest.listFiles(new ClassFilter());
		
		if(tmp != null && tmp.length != 0) {
			for(var file : tmp) {
				if(file.isFile()) {
					result.add(toClassName(file, start));
				}else {
					result.addAll(scanClasses(file, start));
				}
			}
		}
		return result;
	}
	
	private static String toClassName(File file, int start) {
		var path = file.getAbsolutePath();
		path = path.substring(start + 1);
		path = path.replaceAll("\\\\", ".");
		return path.replaceAll("/", ".").replace(".class", "");
	}
	
	public static Object cast(Object v, Type t) {
		if(!(v instanceof String)) {
			return null;
		}
		return cast(t, (String)v);
	}
	
	public static Object cast(Type t, String v) {
		var name = t.getTypeName();
		try {
			switch(name) {
				case "java.lang.String":
					return v;
				case "java.lang.Integer":
					return Integer.parseInt(v);
				case "java.lang.Float":
					return Float.parseFloat(v);
				case "java.lang.Double":
					return Double.parseDouble(v);
				case "java.lang.Long":
					return Long.parseLong(v);
				case "java.lang.Boolean":
					return Boolean.parseBoolean(v);
				case "java.lang.Short":
					return Short.parseShort(v);
				case "java.lang.Byte":
					return Byte.parseByte(v);
				case "int":
					return Integer.parseInt(v);
				case "float":
					return Float.parseFloat(v);
				case "double":
					return Double.parseDouble(v);
				case "long":
					return Long.parseLong(v);
				case "boolean":
					return Boolean.parseBoolean(v);
				case "short":
					return Short.parseShort(v);
				case "byte":
					return Byte.parseByte(v);
				default:
					throw Panic.unsupportedType(name);
			}
		}catch( NumberFormatException e) {
			throw Panic.typeConvertError(name, v, e);
		}
	}
	
	private static final Map<String, Integer> PRIMITIVES = new HashMap<>() {/**
		 * 
		 */
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
		put("java.lang.String",    9); //Total 9 kinds of primitive types.
	}};
			
	
	public static boolean isPrimitive(Class<?> clazz) {
		var name = clazz.getName();
		return PRIMITIVES.containsKey(name);
	}
	
	public static boolean compareTypes(String actual, String expect) {
		if(expect.equals(actual)) return true;
		return PRIMITIVES.get(actual) == PRIMITIVES.get(expect);
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
	
	public static Logger getLogger(Class<?> clazz) {
		return LogManager.getLogger(clazz);
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
