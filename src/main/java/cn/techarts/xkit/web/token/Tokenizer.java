package cn.techarts.xkit.web.token;

public interface Tokenizer {
	/**
	 * Generate a token.
	 * 
	 *@param ip The client ip address
	 *@param userId The unique user identifer (a number, an email or a mobile number...)
	 *@param ua User-Agent
	 *@param key The secret key (Required)
	 *@param salt The extra string making the token stronger
	 *@param duration The max alive of the token
	 */
	public String create(String ip, String userId, String ua, String salt, int duration, String key);
	
	/**
	 * Decode and verify the token.
	 * 
	 *@param ip The client ip address. (Optional)
	 *@param userId The unique user identifer (a number, an email or a mobile number...)
	 *@param ua User-Agent
	 *@param key The secret key (Required)
	 *@param salt The extra string making the token stronger
	 *@param duration The max alive of the token
	 *@param token The token past from client. (Required)
	 *
	 */
	public boolean verify(String ip, String userId, String ua, String salt, int duration, String key, String token);
}
