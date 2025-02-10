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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import cn.techarts.dragonfly.app.helper.Empty;
import cn.techarts.dragonfly.util.Hotpot;

/**
 * @author rocwon@gmail.com
 */
public class ParameterHelper {
	private Map<Integer, SqlMeta> cachedStatements = null;
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public ParameterHelper() {
		this.cachedStatements = new HashMap<>(512);
	}
	
	/** 0: DBUTILS, 1: OPENJPA */
	protected SqlMeta parseStatement(String sql, int type) {
		var key = Integer.valueOf(sql.hashCode());
		var result = this.cachedStatements.get(key);
		if(Objects.isNull(result)) {
			if(type == 0) { //DBUTILS
				result = parseNamedParameters(sql);
			}else {			//OPENJPA
				result = parseHjqlNamedParameters(sql);
			}
			if(result != null) cachedStatements.put(key, result);
		}
		if(Objects.isNull(result) || !result.check()) {
			throw new DataException("Could not find the sql: " + sql);
		}
		LOGGER.info("Executing the SQL statement: " + result.getSql());
		return result;
	}
	
	public static SqlMeta parseNamedParameters(String sql) {
		if(Empty.is(sql)) return null;
		var chars = sql.toCharArray();
		var length = chars.length;
		var matched = false;
		var param = new StringBuilder(24);
		var stmt = new StringBuilder(256);
		var params = new ArrayList<String>();
		for(int i = 0; i < length; i++) {
			char ch = chars[i];
			if(matched) {
				if(ch != '}') {
					param.append(ch);
				}else { //End of the parameter
					matched = false;
					params.add(param.toString());
					param = new StringBuilder(24);
				}
			}else {
				stmt.append(ch);
				if(ch != '#') continue;
				if(chars[i++ + 1] != '{') continue;
				stmt.setCharAt(stmt.length() - 1, '?');
				matched = true; //Start of a parameter
			}
		}
		return new SqlMeta(stmt.toString(), params);
	}
		
	public static SqlMeta parseHjqlNamedParameters(String sql) {
		if(Empty.is(sql)) return null;
		var chars = sql.toCharArray();
		var length = chars.length;
		var matched = false;
		var param = new StringBuilder(24);
		var stmt = new StringBuilder(256);
		var params = new ArrayList<String>();
		for(int i = 0; i < length; i++) {
			char ch = chars[i];
			if(matched) {
				if(ch != ' ' && ch != ',') {
					param.append(ch);
				}else { //End of the parameter
					matched = false;
					stmt.append(ch); //Blank
					params.add(param.toString());
					param = new StringBuilder(24);
				}
			}else {
				stmt.append(ch);
				if(ch != ':') continue;
				stmt.setCharAt(stmt.length() - 1, '?');
				matched = true; //Start of a parameter
			}
		}
		params.add(param.toString());
		return new SqlMeta(stmt.toString(), params);
	}
	
	public static class SqlMeta{
		private String sql;
		private List<String> args;
		
		public SqlMeta(String statement, List<String> params) {
			this.setArgs(params);
			this.setSql(statement);
		}
		
		public boolean check() {
			return sql != null && !sql.isBlank();
		}
		
		/**
		 * The parameters number count
		 */
		public int count() {
			return args != null ? args.size() : 0;
		}
		
		public boolean hasArgs() {
			return args != null && !args.isEmpty();
		}	
		
		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}
		
		public List<String> getArgs() {
			return args;
		}

		public void setArgs(List<String> args) {
			this.args = args;
		}
		
		public Object[] toParameters(Object arg) {
			int count = this.count();
			if(arg == null || count == 0) return null;
			LOGGER.info("====> Parameters: " + arg);
			if(count == 1) {//1 parameter & Primitive Type
				if(arg instanceof Number) return new Object[] {arg};
				if(arg instanceof String) return new Object[] {arg};
				if(arg instanceof Boolean) return new Object[] {arg};
				if(arg instanceof Character) return new Object[] {arg};
			}
			var result = new Object[count];
			for(int i = 0; i < result.length; i++) {
				result[i] = getValue(arg, args.get(i));
			}
			return result;
		}
		
		public Map<String, Object> toParameters2(Object arg){
			int count = this.count();
			if(arg == null || count == 0) return null;
			LOGGER.info("====> Parameters: " + arg);
			var result = new HashMap<String, Object>();
			for(int i = 0; i < count; i++) {
				var name = args.get(i);
				result.put(name, getValue(arg, name));
			}
			return result;
		}
		
		
		private Object getValue(Object obj, String field) {
			if(Empty.or(obj, field)) return null;
			var method = toMethodName("get", field);
			try {
				var raw = obj.getClass();
				var getter = raw.getMethod(method);
				if(Objects.isNull(getter)) return null;
				return getter.invoke(obj);
			}catch(Exception e) {
				throw new DataException("Failed to get value.", e);
			}
		}
		
		private String toMethodName(String prefix, String field) {
			var chars = field.toCharArray();
			if (chars[0] >= 'a' && chars[0] <= 'z') {
				chars[0] = (char) (chars[0] - 32);
			}
			return prefix.concat(String.valueOf(chars));
		}
	}
}