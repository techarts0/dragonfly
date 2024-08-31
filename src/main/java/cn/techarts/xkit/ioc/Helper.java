package cn.techarts.xkit.ioc;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities
 */
public final class Helper {
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
