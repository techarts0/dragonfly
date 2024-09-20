package cn.techarts.xkit.data.trans;

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
