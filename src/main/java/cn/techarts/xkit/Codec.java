package cn.techarts.xkit;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Codec {
	private static ObjectMapper jcodec = null, ccodec = null;
	
	static {
		ccodec = new ObjectMapper();
		jcodec = new ObjectMapper();
		ccodec.setSerializationInclusion(Include.NON_DEFAULT);
		jcodec.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		ccodec.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		jcodec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ccodec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public static String toJson(Object src)  throws RuntimeException{
		if(src == null) return null;
		try{
			return jcodec.writeValueAsString(src);
		}catch( Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Encodes to JSON and ignores all properties which value is NULL or 0 to reduce the size. 
	 */
	public static String toCompactJson(Object src)  throws RuntimeException{
		if(src == null) return null;
		try{
			return ccodec.writeValueAsString(src);
		}catch( Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static<T> T decodeJson(String src, Class<T> targetClass) throws RuntimeException {
		try {
			return jcodec.readValue(src, targetClass);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}