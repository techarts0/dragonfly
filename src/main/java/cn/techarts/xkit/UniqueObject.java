package cn.techarts.xkit;

/**
 * The property "owner" defines the ownership of the object or data.<p>
 * SaaS is a very popular way for software distribution, If a class is derived from 
 * this class {@link UniqueObject}, it supports the multiple-tenants and cloud environment. 
 */
public class UniqueObject extends IdObject {
	
	private static final long serialVersionUID = 1L;

	public static final int PAGESIZE = 20;
	
	public static final float ZERO = 0.00001f;
	public static final int INFINITE = 10000;
	
	private int owner;
	private String name;
	
	private int offset;
	
	private int psize = PAGESIZE;
	
	public UniqueObject() {}
	
	public UniqueObject(int id, String name) {
		this.setId(id);
		this.name = name;
	}
	
	public UniqueObject(int owner, int id, String name) {
		this(id, name);
		this.owner = owner;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * If offset < 0, set a very large number to psize
	 */
	public void setOffset(int offset) {
		if(offset < 0) this.psize = 100000;
		this.offset = offset >= 0 ? offset : 0;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public void setPsize(int psize) {
		if(psize < -1000) {
			this.psize = 0;
			return;
		}
		if(psize < 0) { 
			this.psize = 10000;
		}else if(psize > 0) {
			this.psize = psize;
		}else {
			this.psize = PAGESIZE;
		}
	}
	
	public int getPsize() {
		return this.psize;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}
	
	
	/**
	 * Try to return all data items which are found
	 */
	public void paginationless() {
		this.offset = 0;
		this.psize = INFINITE;
	}
	
	/**
	 * The page number starts default from 0 if you ignored the @param start.
	 */
	public void setPage(int page, int... start) {
		var def = start == null || start.length == 0;
		var firstPageNumber = def ? 0 : start[0];
		this.offset = this.psize * firstPageNumber;
	}
}