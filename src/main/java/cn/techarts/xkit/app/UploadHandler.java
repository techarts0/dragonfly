package cn.techarts.xkit.app;

import java.io.InputStream;

/**
 * The implementation needs to be managed by IoC container using the annotation @Named(UploadHandler.ID)<p>
 * For example:<p>
 * 
 * @Named(FileUploader.ID)<br>
 * public class FileUploadService extends AbstractService implements FileUploadService, UploadHandler{<br>
 *     //Implementations ...<br>
 * }
 */
public interface UploadHandler {
	public static final String ID = "cn.techarts.xkit.app.UploadHandler";
	
	/**
	 * @param name The original file name you uploaded.
	 */
	public void handle(String name, String type, long size, InputStream content);
}
