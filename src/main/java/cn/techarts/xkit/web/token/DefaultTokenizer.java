package cn.techarts.xkit.web.token;

import cn.techarts.xkit.app.helper.Converter;
import cn.techarts.xkit.app.helper.Cryptor;

public class DefaultTokenizer implements Tokenizer{

	@Override
	public String create(String ip, String userId, String ua, String salt, int duration, String key) {
		var minutes = String.valueOf(minutes());
		var tmp = (ip != null ? ip : "0000") + userId;
		tmp = minutes.concat(tmp).concat(salt).concat(ua);
		return Cryptor.encrypt(tmp, Cryptor.toBytes(key));
	}

	@Override
	public boolean verify(String ip, String userId, String ua, String salt, int duration, String key, String token) {
		var tmp = Cryptor.decrypt(token, Cryptor.toBytes(key));
		if(tmp == null) return false; //An invalid token
		var bgn = Converter.toInt(tmp.substring(0, 8));
		if(minutes() - bgn > duration) return false;
		var result = (ip != null ? ip : "0000") + userId + salt + ua;
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public static int minutes() {
		return (int)(System.currentTimeMillis() / 60000);
	}

}
