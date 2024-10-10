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

/**
 * The annotation is the same as @MediaType in JSR370.
 * @author rocwon@gmail.com
 */
public enum MediaType {
	NONE(null),
	TEXT("text/plain"),
	HTML("text/html"),
	PNG("image/png"),
	JPEG("image/jpeg"),
	XML("application/xml"),
	JSON("application/json;charset=UTF-8"),
	FORM("application/x-www-form-urlencoded");
	
	private String contentType;
	
	MediaType(String contentType){
		this.contentType = contentType;
	}

	public String value() {
		return contentType;
	}	
}
