package cn.techarts.xkit.app;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Codec {
	private static ObjectMapper jcodec = null;
	
	static {
		jcodec = new ObjectMapper();
		jcodec.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		jcodec.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		jcodec.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
		jcodec.setSerializationInclusion(Include.NON_DEFAULT);
		try{
			return jcodec.writeValueAsString(src);
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