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
public interface Advice {
	
	/**
	 * @param args The parameters of target method you passed.[All Advice]
	 * @param result The return value of the target method.[After Advice]
	 * @param e An exception is threw if it's not NULL. [Threw Advice]
	 */
	public void execute(Object[] args, Object result, Throwable e);
}
