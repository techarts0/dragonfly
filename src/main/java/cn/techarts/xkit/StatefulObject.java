package cn.techarts.xkit;

public class StatefulObject extends UniqueObject {
	
	private static final long serialVersionUID = 1L;
	
	private int status;

	public StatefulObject() {}
	
	public StatefulObject(int id) {
		this.setId(id);
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public StatefulObject withStatus(int status) {
		this.status = status;
		return this;
	}
	
}
