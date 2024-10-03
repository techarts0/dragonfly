/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * 
 * @author rocwon@gmail.com
 */
public interface UploadHandler {
	public static final String ID = "cn.techarts.xkit.app.UploadHandler";
	
	/**
	 * @param name The original file name you uploaded.
	 */
	public void handle(String name, String type, long size, InputStream content);
}
