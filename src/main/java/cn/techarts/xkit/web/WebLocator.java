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

package cn.techarts.xkit.web;

import java.util.Map;
import java.util.Objects;

import cn.techarts.whale.util.Hotpot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author rocwon@gmail.com
 */
public class WebLocator {
	private ServiceMeta meta = null;
	private boolean wildcard = false;
	private Map<String, WebLocator> values;
	private static final String DELIMITER = "/";
	private static final String WILDCARD = "////";
	
//	private Map<String, Integer> pathParams;
//	private int currentPathParamIndex = 0;
	
	public WebLocator(boolean wildcard) {
		this.wildcard = wildcard;
		this.values = new HashMap<>(8);
		//if(!wildcard || locator == null) return;
		//this.pathParams = new HashMap<>(4);
		//var end = locator.length() - 1;
		//var loc = locator.substring(1, end); // {XXX}
		//pathParams.put(loc, currentPathParamIndex++);
	}
	
//	public int getPathParamIndex(String name) {
//		if(pathParams == null) return -1;
//		var result = pathParams.get(name);
//		return result == null ? -1 : result.intValue();
//	}
	
	// Pattern: POST/authors/{id}/books/{isbn}
	public int parse(ServiceMeta meta) {
		if(Objects.isNull(meta)) return 0;
		var uri = meta.getConcreteUri();
		if(!meta.isRestful()) {//Classic URL
			var wl = new WebLocator(false);
			values.put(uri, wl.setServiceMeta(meta));
		}else { //Restful Pattern URL
			var locators = uri.split(DELIMITER);
			var length = locators.length;
			WebLocator current = this; //First Locator
			for(int i = 0; i < length; i++) {
				var wc  = wild(locators[i]);
				var loc = wc ? WILDCARD : locators[i];
				var next = current.get(loc);
				if(Objects.isNull(next)) {
					next = new WebLocator(wc);
					current.put(loc, next);
				}
				current = next; //Skip to next
			}
			current.setServiceMeta(meta); //The last
		}
		return 1;
	}
	
	// Pattern: POST/authors/1/books/233
	public ServiceMeta matches(String uri, String method) {
		if(Hotpot.orNull(uri, values)) return null;
		var result = values.get(uri);
		if(result != null) {//Classic URL
			return result.getServiceMeta();
		}
		var url = method.concat(uri);
		var arguments = new ArrayList<String>();
		var parts = url.split(DELIMITER);
		var len = parts.length - 1;
		var current = this; //First Locator
		for(int i = 0; i <= len; i++) {
			var next = current.get(parts[i]);
			if(next == null && i != len) {
				return null; //Could not find
			}else {
				current = next; //To the next
				if(current.isWild()) {
					arguments.add(parts[i]);
				}
			}
		}
		return current.getServiceMeta(arguments);
	}	
	
	private WebLocator setServiceMeta(ServiceMeta meta) {
		this.meta = meta;
		this.values = null;
		return this;
	}
	
	public ServiceMeta getServiceMeta(List<String> arguments) {
		if(Objects.isNull(meta)) return null;
		return this.meta.setArguments(arguments);
	}
	
	public ServiceMeta getServiceMeta() {
		return this.meta;
	}
	
	private boolean wild(String locator) {
		return locator.startsWith("{") && locator.endsWith("}");
	}
	
	/**
	 * Is wildcard.
	 */
	public boolean isWild() {
		return this.wildcard;
	}
	
	public void put(String key, WebLocator val) {
		this.values.put(key, val);
	}
	
	public WebLocator get(String val) {
		if(Hotpot.orNull(val, values)) return null;
		var result = values.get(val);
		if(result != null) return result;
		return values.get(WILDCARD);
	}
	
	public boolean hasNext() {
		if(meta == null) return false;
		if(values == null) return true;
		return this.values.isEmpty();
	}
}