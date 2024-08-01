package cn.techarts.xkit;

import java.io.Serializable;

/**
 * Generally, a POJO should derived from the class {@link IdObject} especially mapping on an entity in database.<p>
 * An instance of a subclass derived from {@link IdObject} owns a unique id(an integer) and supports serialization.
 */
public class IdObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * In some languages such as OBJECT-C or PYTHON, the "id" is reserved as a keyword.
	 * Programmer could not use it in their codes, so we provide a redundant property named "xid". 
	 */
	public int getXid() {
		return id;
	}
}