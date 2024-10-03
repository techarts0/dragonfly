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

package cn.techarts.xkit.app;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author rocwon@gmail.com
 */
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