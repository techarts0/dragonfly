package cn.techarts.xkit.helper;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;

/**
 * Slice the array or collection from the specified indexes start(inclusive) to end(exclusive).
 */
public final class Slicer {
	
	private static int getEndIndex(int end, int length) {
		return end < length ? end : length - 1;
	}
	
	public static int[] slice(int[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new int[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public static float[] slice(float[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new float[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public static double[] slice(double[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new double[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public static long[] slice(long[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new long[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public static boolean[] slice(boolean[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new boolean[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public static byte[] slice(byte[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new byte[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public static char[] slice(char[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new char[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	public String slice(String arg, int bgn, int end) {
		if(Empty.is(arg)) return null;
		var chars = slice(arg.toCharArray(), bgn, end);
		return chars != null ? String.valueOf(chars) : null;
	}
	
	public static Object[] slice(Object[] arg, int start, int end) {
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new Object[endIndex - start];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static<T> T[] slice(T[] arg, int start, int end, Class<T> clazz) {
		if(Empty.is(arg) || end < start) return null;
		var length = getEndIndex(end, arg.length) - start;
		var result = Array.newInstance(clazz, length);
		System.arraycopy(arg, start, result, 0, length);
		return (T[])result; //Force to convert the generic type
	}
	
	public static<T> List<T> slice(List<T> arg, int start, int end){
		if(Empty.is(arg) || end < start) return null;
		var endIndex = getEndIndex(end, arg.size());
		var result = arg.subList(start, endIndex);
		result.add(arg.get(endIndex)); //Because the index end is exclusive
		return result;
	}
	
	public static<T> Set<T> slice(Set<T> arg, int start, int end){
		if(Empty.is(arg)) return null;
		var tmp = slice(List.copyOf(arg), start, end);
		return tmp != null ? Set.copyOf(tmp) : null;
	}
}