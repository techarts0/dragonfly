package cn.techarts.xkit;

public enum Results {
	Success(0),
	Failure(-1),
	RemoteFault(-1212);
	
	private int id = 0;
	
	private Results(int id) {
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public static boolean isOK(int errorCode) {
		return errorCode == Success.getId();
	}
	
	public static final int SUCCESS = 0;
	
	/**Failure*/
	public static final Integer FAILURE = Integer.valueOf(-1);
}
