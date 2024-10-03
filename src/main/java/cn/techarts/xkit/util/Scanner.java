package cn.techarts.xkit.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import cn.techarts.xkit.data.DataException;
import jakarta.persistence.Entity;
/**
 * Scan the packages under the given base class-path.
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
		if(files == null || files.length == 0) return null;
		try {
			List<String> result = new ArrayList<>(24);
			for(var f : files) {
				if(f == null || !f.isFile()) continue;
				var n = f.getName().replace(".class", "");
				var c = Class.forName(pkg.concat(".").concat(n));
				if(c == null) continue;
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
	
	public XFileFilter( String fileType)
	{
		this.type = fileType;
	}
	
	@Override
	public boolean accept( File file)
	{
		if( file == null) return false;
		if( this.type == null || this.type.isEmpty()) return true;
		return file.isFile() && file.getName().endsWith( this.type);
	}
}