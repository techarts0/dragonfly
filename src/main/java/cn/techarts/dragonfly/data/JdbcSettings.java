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

package cn.techarts.dragonfly.data;

import javax.inject.Inject;

import cn.techarts.whale.Valued;

/**
 * @author rocwon@gmail.com
 */
public class JdbcSettings {
	@Inject
	@Valued(key="jdbc.url")
	protected String url;
	
	@Inject
	@Valued(key="jdbc.username")
	protected String user;
	
	@Inject
	@Valued(key="jdbc.driver")
	protected String driver;
	
	@Inject
	@Valued(key="jdbc.password")
	protected String password;
	
	@Inject
	@Valued(key="jdbc.capacity")
	protected int capacity;
	
	@Inject
	@Valued(key="jdbc.framework")
	protected String framework;
	
	//For MyBatis and OPENJPA
	@Inject
	@Valued(key="jdbc.model.package")
	protected String modelPackage;
}