package cn.techarts.dragonfly.web.jsonrpc;

public class JsonRpcError extends JsonRpcMessage{
	private Error error;
	public static final int PARSE_ERROR = -32700;
	public static final int INVALID_REQUEST = -32600;
	public static final int METHOD_NOT_FOUND = -32601;
	public static final int INVALID_PARAMS = -32602;
	public static final int INTERNAL_ERROR = -32603;
	
	public JsonRpcError(int id) {
		this.setId(id);
	}
	
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}
	
	public void error(int code, String msg) {
		this.error = new Error();
		error.setCode(code);
		error.setMessage(msg);
	}
	
	public void setErrorData(Object data) {
		if(error == null) return;
		this.error.setData(data);
	}
	
	public class Error{
		private int code;
		private String message;
		private Object data;
		
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public Object getData() {
			return data;
		}
		public void setData(Object data) {
			this.data = data;
		}
	}
}