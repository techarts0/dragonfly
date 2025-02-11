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
package cn.techarts.dragonfly.web.token;

import cn.techarts.dragonfly.app.helper.Converter;
import cn.techarts.dragonfly.app.helper.Cryptor;

/**
 * @author rocwon@gmail.com
 */
public class DefaultTokenizer implements Tokenizer{

	@Override
	public String create(ClientContext client, TokenConfig config) {
		var minutes = String.valueOf(minutes());
		var source = minutes.concat(client.getIp())
							.concat(client.getUser())
							.concat(client.getUa())
							.concat(config.getSalt());
		return Cryptor.encrypt(source, Cryptor.toBytes(config.getKey()));
	}

	@Override
	public boolean verify(ClientContext client, TokenConfig config, String token) {
		var tmp = Cryptor.decrypt(token, Cryptor.toBytes(config.getKey()));
		if(tmp == null) return false; //An invalid token
		var bgn = Converter.toInt(tmp.substring(0, 8));
		if(minutes() - bgn > config.getDuration()) return false;
		var result = client.getIp()
						   .concat(client.getUser())
						   .concat(client.getUa())
						   .concat(config.getSalt());						    
		return result != null ? result.equals(tmp.substring(8)) : false;
	}
	
	public static int minutes() {
		return (int)(System.currentTimeMillis() / 60000);
	}

}
