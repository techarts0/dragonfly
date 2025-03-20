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
import java.util.Random;
import cn.techarts.dragonfly.util.Codec;
import cn.techarts.dragonfly.app.helper.Requester;

/**
 * An implementation of RPC based on HTTP & JSONRPC & JWT
 * @author rocwon@gmail.com
 */
public class JsonRpcClient {
	private String url;
	private String token;
	private Random random;
	
	public JsonRpcClient(String remoteUrl, String token) {
		this.token = token;
		this.url = remoteUrl;
	}
	
	private Map<String, String> getHeaders(){
		Map<String, String> result = new HashMap<>();
		if(token != null){
			result.put("Authorization", "Bearer " + token);
		}
		result.put("Content-Type", "application/json;charset=UTF-8");
		return result;
	}
	
	public JsonRpcMessage get(String method, Map<String, Object> params, int id) {
		var request = new JsonRpcRequest();
		request.setId(id);
		request.setMethod(method);
		request.setParams(params);
		var result = Codec.toJson(request);
		return decode(Requester.get(url, result, getHeaders()));
	}
	
	public JsonRpcMessage post(String method, Map<String, Object> params, int id) {
		var request = new JsonRpcRequest();
		request.setId(id);
		request.setMethod(method);
		request.setParams(params);
		var result = Codec.toJson(request);
		return decode(Requester.post(url, result, getHeaders()));
	}
	
	private JsonRpcMessage decode(String json) {
		if(json == null || json.isEmpty()) return null;
		if(json.contains("\"result\":")) {
			return Codec.decodeJson(json, JsonRpcResult.class);
		}else {
			return Codec.decodeJson(json, JsonRpcError.class);
		}
	}
	
	public int id() {
		if(random == null) {
			random = new Random(System.currentTimeMillis());
		}
		return random.nextInt(1, Integer.MAX_VALUE);
	}
	
	public void setUrl(String remoteUrl) {
		this.url = remoteUrl;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
}