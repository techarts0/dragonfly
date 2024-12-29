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

package cn.techarts.xkit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.techarts.xkit.app.helper.Empty;
import cn.techarts.xkit.data.DataException;
import jakarta.persistence.Entity;
/**
 * Internal utility.
 * 
 * Scan the packages under the given base class-path.
 * @author rocwon@gmail.com
 */
public class Scanner {
	private String base;
	private String pkg;
	
	public Scanner(String base, String pkg) {
		this.pkg = pkg;
		this.base = base;
	}
	
	private static File[] poll( String srcFolder, String fileType){
		var directory = new File( srcFolder);
		return directory.listFiles( new XFileFilter(fileType));
	}
	
	public List<String> scanJPAEntities() {
		var path = base.concat(pkg.replace('.', '/'));
		var files = poll(path, ".class");
		if(Empty.is(files)) return null;
		try {
			List<String> result = new ArrayList<>(24);
			for(var f : files) {
				if(Objects.isNull(f) || !f.isFile()) continue;
				var n = f.getName().replace(".class", "");
				var c = Class.forName(pkg.concat(".").concat(n));
				if(Objects.isNull(c)) continue;
				if(!c.isAnnotationPresent(Entity.class)) continue;
				result.add(c.getName());
			}
			return result;
		}catch(Exception e) {
			throw new DataException("Failed to scan JPA entities.", e);
		}
	}
	
	public static List<String> scanClasses(File dest, int start){
		var result = new ArrayList<String>();
		var tmp = dest.listFiles(new ClassFilter());
		
		if(tmp != null && tmp.length != 0) {
			for(var file : tmp) {
				if(file.isFile()) {
					result.add(toClassName(file, start));
				}else {
					result.addAll(scanClasses(file, start));
				}
			}
		}
		return result;
	}
	
	private static String toClassName(File file, int start) {
		var path = file.getAbsolutePath();
		path = path.substring(start + 1);
		path = path.replaceAll("\\\\", ".");
		return path.replaceAll("/", ".").replace(".class", "");
	}
}

class XFileFilter implements FileFilter
{
	private String type = null;
	
	public XFileFilter( String fileType){
		this.type = fileType;
	}
	
	@Override
	public boolean accept( File file){
		if( Objects.isNull(file)) return false;
		if(Empty.is(this.type)) return true;
		return file.isFile() && file.getName().endsWith( this.type);
	}
}