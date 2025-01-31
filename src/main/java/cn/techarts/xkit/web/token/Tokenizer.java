package cn.techarts.xkit.web.token;

public interface Tokenizer {
	/**
	 * Generate a token.
	 */
	public String create(ClientContext client, TokenConfig config);
	
	/**
	 * Decode and verify the token.
	 *@param token The token past from client. (Required)
	 */
	public boolean verify(ClientContext client, TokenConfig config, String token);
}