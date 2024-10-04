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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author rocwon@gmail.com
 */
public class WebResource {
	private boolean wildcard = false;
	private ServiceMeta service = null;
	private Map<String, WebResource> values;
	private static final String DELIMITER = "/";
	private static final String WILDCARD = "////";
	
	public WebResource(boolean wildcard) {
		this.wildcard = wildcard;
		this.values = new HashMap<>(32);
	}
	
	// Pattern: POST/authors/{id}/books/{isbn}
	public void parse(ServiceMeta meta) {
		var uri = meta.getConcreteUri();
		if(!meta.isRestful()) {//Classic URL
			var wl = new WebResource(false);
			values.put(uri, wl.setServiceMeta(meta));
		}else { //Restful Pattern URL
			var locators = uri.split(DELIMITER);
			var length = locators.length;
			WebResource current = this; //First Locator
			for(int i = 0; i < length; i++) {
				var wc  = wild(locators[i]);
				var loc = wc ? WILDCARD : locators[i];
				var next = current.get(loc);
				if(next == null) {
					next = new WebResource(wc);
					current.put(loc, next);
				}
				current = next; //Skip to next
			}
			current.setServiceMeta(meta); //The last
		}
	}
	
	private WebResource setServiceMeta(ServiceMeta meta) {
		this.service = meta;
		this.values = null;
		return this;
	}
	
	public ServiceMeta getServiceMeta(List<String> arguments) {
		return this.service.setArguments(arguments);
	}
	
	public ServiceMeta getServiceMeta() {
		return this.service;
	}
	
	private boolean wild(String locator) {
		return locator.startsWith("{") && locator.endsWith("}");
	}
	
	/**
	 * Is wild card.
	 */
	public boolean isWild() {
		return this.wildcard;
	}
	
	public void put(String key, WebResource val) {
		this.values.put(key, val);
	}
	
	public WebResource get(String val) {
		if(val == null) return null;
		var result = values.get(val);
		if(result != null) return result;
		return values.get(WILDCARD);
	}
	
	public boolean hasNext() {
		if(service == null) return false;
		if(this.values == null) return true;
		return this.values.isEmpty();
	}
	
	// Pattern: POST/authors/1/books/233
	public ServiceMeta matches(String uri, String method) {
		if(uri == null) return null;
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
}