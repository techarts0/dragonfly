package cn.techarts.xkit.data;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.io.UnsupportedEncodingException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.BaseTypeHandler;

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
		if(blob == null || blob.length() == 0) return null;
        try {
        	 var result = blob.getBytes(1, (int) blob.length());  
        	 return new String(result, DEFAULT_CHARSET);
        }catch (UnsupportedEncodingException e) {  
       	 throw new RuntimeException("Failed to encode blob value.");  
       } 
	}
	
}