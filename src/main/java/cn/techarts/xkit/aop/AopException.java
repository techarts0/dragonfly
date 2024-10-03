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

package cn.techarts.xkit.aop;

/**
 * @author rocwon@gmail.com
 */
public class AopException extends RuntimeException {
	
	private static final long serialVersionUID = -6501295436814757979L;

	public AopException(String cause) {
		super(cause);
	}
	
	public AopException(String cause, Throwable throwable) {
		super(cause, throwable);
	}
	
	public static AopException nullTarget() {
		return new AopException("The target object is null.");
	}
	
	public static AopException nullAdvice() {
		return new AopException("The advice object is null.");
	}
	
	public static AopException notAnInterface(Class<?> t) {
		return new AopException("The parameter t is not an interface: " + t.getName());
	}
	
	public static AopException notFound(String clzz, Throwable e) {
		return new AopException("Can not find the class or method: " + clzz, e);
	}
	
	public static AopException failedSaveFile(Throwable e) {
		return new AopException("Failed to save the class bytecode.", e);
	}
	
	
}