package cn.techarts.xkit.web.token;

import cn.techarts.xkit.app.helper.Converter;
import cn.techarts.xkit.app.helper.Cryptor;

public class DefaultTokenizer implements Tokenizer{

	@Override
	public String create(ClientContext client, TokenConfig config) {
		var minutes = String.valueOf(minutes());
		var tmp = (client.getIp() != null ? client.getIp() : "0000") + client.getUser();
		tmp = minutes.concat(tmp).concat(config.getSalt()).concat(client.getUa());
		return Cryptor.encrypt(tmp, Cryptor.toBytes(config.getKey()));
	}

	@Override
	public boolean verify(ClientContext client, TokenConfig config, String token) {
		var tmp = Cryptor.decrypt(token, Cryptor.toBytes(config.getKey()));
		if(tmp == null) return false; //An invalid token
		var bgn = Converter.toInt(tmp.substring(0, 8));
		if(minutes() - bgn > config.getDuration()) return false;
		var result = (client.getIp() != null ? client.getIp() : "0000") + 
					 client.getUser() + config.getSalt() + client.getUa();
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public static int minutes() {
		return (int)(System.currentTimeMillis() / 60000);
	}

}
