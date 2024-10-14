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

import java.util.Objects;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author rocwon@gmail.com
 */
public class ServiceInitializer implements ServletContainerInitializer{
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		var classpath = this.getRootClassPath();
		new ServiceEnhancer(classpath).start();
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(Objects.isNull(result) || result.getPath() == null) {
			throw new RuntimeException("Failed to get resource path.");
		}
		return result.getPath(); //Usually, it is WEB-INF/classes
	}
}