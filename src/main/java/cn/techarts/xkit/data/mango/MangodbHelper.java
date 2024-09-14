package cn.techarts.xkit.data.mango;

import javax.inject.Inject;

import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import cn.techarts.xkit.app.UniObject;
import cn.techarts.xkit.data.DataException;
import cn.techarts.xkit.ioc.Valued;

//TODO
public class MangodbHelper implements AutoCloseable{
	
	@Inject
	@Valued(key="mangodb.url")
	private String url;
	
	private MongoClient client;
	private MongoDatabase database;
	private ObjectMapper converter;
	
	public MangodbHelper(){
		if(this.url == null) return;
		this.converter = new ObjectMapper();
		this.client = MongoClients.create(url);
	}

	public void switchDatabase(String database) {
		this.database = client.getDatabase(database);
	}
	
//	public void save(Object obj, String table) {
//		if(obj == null || table == null) return;
//		var target = database.getCollection(table);
//		if(target != null) {
//			var param = this.to(obj);
//			var _id = param.get("_id");
//			
//			if(_id == null) {
//				var result = target.insertOne(to(obj));
//				return result != null ? result.
//			}else {
//				var result = target.updateOne(new Document("_id", _id), param);
//				return result.getModifiedCount();
//			}
//			
//			
//		}
//	}
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	//Object to Document
	private Document to(Object obj) {
		try {
			var json = converter.writeValueAsString(obj);
			return Document.parse(json);
		}catch(Exception e) {
			throw new DataException("Failed to convert pojo to document.", e);
		}
	}
	
	private<T> T from(Document doc, Class<T> clazz) {
		try {
			return converter.readValue(doc.toJson(), clazz);
		}catch(Exception e) {
			throw new DataException("Failed to convert document to pojo.", e);
		}
	}
}