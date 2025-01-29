package cn.techarts.xkit.data.mango;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import cn.techarts.whale.Valued;
import cn.techarts.xkit.data.DataException;

@Singleton
public class MangodbExecutor implements AutoCloseable{
	
	@Inject
	@Valued(key="mangodb.url")
	private String url;
	
	private MongoClient client;
	private MongoDatabase database;
	private ObjectMapper converter;
	
	public MangodbExecutor(){
		if(this.url == null) return;
		this.converter = new ObjectMapper();
		this.client = MongoClients.create(url);
	}

	public void switchDatabase(String database) {
		this.database = client.getDatabase(database);
	}
	
	private MongoCollection<Document> getTable(String table){
		var result = database.getCollection(table);
		if(result == null) {
			throw new RuntimeException("Collection does not exist: " + table);
		}
		return result;
	}
	
	/**
	 * @param table: collection
	 */
	public long save(Object obj, String table) {
		if(obj == null || table == null) return 0;
		var target = getTable(table);
		var param = this.to(obj);
		var _id = param.get("_id");
			
		if(_id == null) {
			var result = target.insertOne(to(obj));
			if(result == null) return 0L;
			return result.getInsertedId().asInt64().getValue();
		}else {
			var result = target.updateOne(new Document("_id", _id), param);
			return result != null ? result.getModifiedCount() : 0;
		}
	}
	
	@Override
	public void close() throws Exception {
		if(this.client != null) {
			this.client.close();
			this.client = null;
		}		
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