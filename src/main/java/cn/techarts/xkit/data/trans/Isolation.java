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

package cn.techarts.xkit.data.trans;

/**
 * @author rocwon@gmail.com
 */
public enum Isolation {
	NONE_TRANSACTION(0),
	READ_UNCOMMITED(1),
	READ_COMMITED(2),
	READ_REPEATABLE(4),
	SERIALIZABLE(8);
	
	private int level;
	
	Isolation(int level){
		this.setLevel(level);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public static Isolation to(int level) {
		if(level == 2) return READ_COMMITED;
		if(level == 1) return READ_UNCOMMITED;
		if(level == 4) return READ_REPEATABLE;
		return level == 8 ? SERIALIZABLE : NONE_TRANSACTION;
	}
	
	public static final int NONE = NONE_TRANSACTION.getLevel();

}
