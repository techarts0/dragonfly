package cn.techarts.dragonfly.web.jsonrpc;

public class JsonRpcMessage {
	private int id;
	private String jsonrpc = "2.0";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getJsonrpc() {
		return jsonrpc;
	}
}