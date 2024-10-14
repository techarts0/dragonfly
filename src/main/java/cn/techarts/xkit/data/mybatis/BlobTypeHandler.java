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

package cn.techarts.xkit.data.mybatis;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.io.UnsupportedEncodingException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.BaseTypeHandler;

/**
 * @author rocwon@gmail.com
 */
public class BlobTypeHandler extends BaseTypeHandler<String> {
    private static final String DEFAULT_CHARSET = "utf-8";  
    
    @Override  
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {  
      ps.setString(i, parameter);
    }  

    @Override  
    public String getNullableResult(ResultSet rs, String columnName)  throws SQLException {  
    	return getStringResult(rs.getBlob(columnName)); 
    }  

    @Override  
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {  
    	return getStringResult(cs.getBlob(columnIndex));
    }

	@Override
	public String getNullableResult(ResultSet arg0, int arg1) throws SQLException {
		return getStringResult(arg0.getBlob(arg1));
	} 
	
	private String getStringResult(Blob blob) throws SQLException{
		if(Objects.isNull(blob) || blob.length() == 0) return null;
        try {
        	 var result = blob.getBytes(1, (int) blob.length());  
        	 return new String(result, DEFAULT_CHARSET);
        }catch (UnsupportedEncodingException e) {  
       	 throw new RuntimeException("Failed to encode blob value.");  
       } 
	}
	
}