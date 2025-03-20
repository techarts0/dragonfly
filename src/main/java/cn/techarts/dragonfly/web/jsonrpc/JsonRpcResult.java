package cn.techarts.dragonfly.web.jsonrpc;

import java.io.Serializable;

public class JsonRpcResult extends JsonRpcMessage implements Serializable{
	private static final long serialVersionUID = 1L;
	private Object result;
	
	public JsonRpcResult(int id, Object data) {
		this.setId(id);
		this.setResult(data);
	}
	
	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
