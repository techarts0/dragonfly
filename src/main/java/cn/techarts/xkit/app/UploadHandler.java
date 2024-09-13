package cn.techarts.xkit.app;

import java.io.InputStream;



/**
 * The implementation needs to be managed by IoC container using the annotation @Named("fileUploadHandler")<p>
 * Important: The name MUST BE "<b>fileUploadHandler</b>".
 * 
 */
public interface UploadHandler {
	
	/**
	 * @param name The original file name you uploaded.
	 */
	public void handle(String name, String type, long size, InputStream content);
}
