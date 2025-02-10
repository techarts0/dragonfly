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

package cn.techarts.dragonfly.app.rpc;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
//import jakarta.inject.Singleton;

import cn.techarts.dragonfly.app.helper.Empty;
import cn.techarts.dragonfly.app.helper.Requester;
import cn.techarts.dragonfly.util.Codec;

/**
 * <p>javax & jakarta</p>
 * RPC based on HTTP & JSON
 * @author rocwon@gmail.com
 */

@Singleton
public class WebRPCHelper {
	public<T> T get(String url, Map<String, String> args, Class<T> t) {
		var result = Requester.get(url, args);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	public<T> T get(String url, Map<String, String> args, Class<T> t, Map<String, String> headers) {
		var result = Requester.get(url, args, headers);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	/**
	 * It is compatible to Dragonfly Web Framework.
	 */
	public<T> T get(String url, Map<String, String> args, Class<T> t, String session) {
		var hdr = new HashMap<String, String>();
		if(session != null) {
			hdr.put("x-session", session);
		}
		var result = Requester.get(url, args, hdr);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	public<T> T post(String url, Map<String, String> args, Class<T> t) {
		var result = Requester.post(url, args);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	/**
	 * It is compatible to Dragonfly Web Framework.
	 */
	public<T> T post(String url, Map<String, String> args, Class<T> t, String session) {
		var hdr = new HashMap<String, String>();
		if(session != null) {
			hdr.put("x-session", session);
		}
		var result = Requester.post(url, args, hdr);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	public<T> T post(String url, Map<String, String> args, Class<T> t, Map<String, String> headers) {
		var result = Requester.post(url, args, headers);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	public<T> T delete(String url, Map<String, String> args, Class<T> t) {
		var result = Requester.delete(url, args);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	/**
	 * It is compatible to Dragonfly Web Framework.
	 */
	public<T> T delete(String url, Map<String, String> args, Class<T> t, String session) {
		var hdr = new HashMap<String, String>();
		if(session != null) {
			hdr.put("x-session", session);
		}
		var result = Requester.delete(url, args, hdr);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	public<T> T put(String url, Map<String, String> args, Class<T> t) {
		var result = Requester.put(url, args);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	/**
	 * It is compatible to Dragonfly Web Framework.
	 */
	public<T> T put(String url, Map<String, String> args, Class<T> t, String session) {
		var hdr = new HashMap<String, String>();
		if(session != null) {
			hdr.put("x-session", session);
		}
		var result = Requester.put(url, args, hdr);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
	
	public<T> T head(String url, Class<T> t) {
		var result = Requester.head(url);
		if(Empty.is(result)) return null;
		return Codec.decodeJson(result, t);
	}
}