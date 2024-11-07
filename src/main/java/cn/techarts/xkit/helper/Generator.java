package cn.techarts.xkit.helper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;

public final class Generator {
	
	/**
	 *@return Returns a unique string that contains 32 chars  
	 */
	public static String uuid() {
		var result = UUID.randomUUID().toString();
		return result.replace("-", "");
	}
	
	private static final String SPECIAL_CHARS = "&%$#@!*_";
	private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXY";
	/**
	 * @return A 6-chars random string that contains letters, numbers and special chars 
	 */
	public static String randomPassword() {
		var rand = new Random();
		var num1 = rand.nextInt(10) + "";;
		var num2 = (rand.nextInt(88) + 10) + "";
		char rc1 = LETTERS.charAt(rand.nextInt(23));
		char rc2 = LETTERS.charAt(rand.nextInt(23));
		char rc3 = SPECIAL_CHARS.charAt(rand.nextInt(8));
		return num2 + (char)(rc1 + 32) + rc3 + num1 + rc2;
	}
	
	/**
	 * @return Returns an integer between 0(inclusive) to max(exclusive)
	 */
	public static int randomNumber(int max) {
		var random = new Random(Time.seconds());
		return random.nextInt(max);
	}
	
	/**
	 * @return CRC16 checksum of the given bytes.
	 */
	public static byte[] CRC16(byte[] bytes) {
		int POLYNOMIAL = 0x0000a001;
		int result = 0x0000ffff, len = bytes.length;
        for (int i = 0; i < len; i++) {
        	result ^= ((int) bytes[i] & 0x000000ff);
            for (int j = 0; j < 8; j++) {
                if ((result & 0x00000001) != 0) {
                	result >>= 1;
            		result ^= POLYNOMIAL;
                } else {
                	result >>= 1;
                }
            }
        }
        return Converter.toBytesLE((short)result);
    }
	
	/**
	 * @return Returns a string (3 ~ 6 chars number)<p>
	 * If you want a picture verification code, please call the method {@link Guarder.getVerificationCode}
	 */
	public static String verificationCode(int length) {
		var result = new StringBuilder("");
		var random = new Random(Time.seconds());
		var len = length <= 3 || length > 6 ? 4 : length;
		for (int i = 0;i < len; i++) {        
			 result.append(random.nextInt(10));
		}
		return result.toString();
	}
	
	/**
	 * The method is design to generate an order-number, a project number etc.
	 * @return Returns a string follows the pattern like: <b>XIAOMI-20211101-0012</b>
	 * @param prefix The leading part of the sequence such as company name, order type
	 * @param postfix The tail part of  the sequence such as a unique number<p>
	 * 
	 * @see Time.format(Date date)
	 */
	public static String dateStyleSequence(String prefix, String postfix) {
		if(Empty.is(prefix) || Empty.is(postfix)) return null;
		var date = Time.format(new Date());
		return new StringBuilder(prefix).append('-')
			   .append(date).append('-').append(postfix).toString();
	}
	
	private static AtomicLong generator = null;
	/**
	 * Generates a continuous number sequence.<p>
	 * Include start but exclude end.
	 */
	public static long seq(long start, long end, boolean reset) {
		if(generator == null || reset) {
			generator = new AtomicLong(start);
		}
		return generator.incrementAndGet();
	}
	
	/**
	 * Generates a random code and draw a picture
	 *@param length The number length of in the picture
	 *@param w, h the width and the height of the picture
	 *@return A string array of the code and picture(base64), e.g.<p>
	 *["1324", "data:image/png;base64,picture-base64-string"] 
	 */
	public static String[] getCaptchaCode(int length, int w, int h) {
		var random = new Random();  
		var type = BufferedImage.TYPE_INT_RGB;
		var image = new BufferedImage(w, h, type);     
	    var g = drawBackground(image, random, w, h);
	    var code = drawVerifyCode(g, random, length);
	    try {
		    var baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();
			var prefix = "data:image/png;base64,";
			var b64 = base64(baos.toByteArray());
			baos.close(); //Close the stream
			return new String[]{code, prefix + b64};
	    }catch(Exception e) {
	    	return null;
	    }
	}
	
	public static String base64(byte[] src) {
		if(Empty.is(src)) return null;
		return new String(Base64.getEncoder().encode(src));
	}
	
	/**
	 * Returns the code and picture using default parameters:<p>
	 * length = 4, width = 60, height = 20
	 */
	public static String[] getCaptchaCode() {
		return getCaptchaCode(4, 60, 20);
	}
	
	private static String drawVerifyCode(Graphics g, Random random, int length){
		var result = new StringBuilder(length);
		for (int i = 0;i < length; i++) {        
			 var ch = String.valueOf(random.nextInt(10));        
			 result.append(ch);
			 g.setColor(getRandColor(random));        
			 g.drawString(ch, 13 * i + 6, 16);     
		}     
		g.dispose();
		return result.toString();
	}
	
	private static Graphics drawBackground(BufferedImage image, Random random, int w, int h){
		 Graphics result = image.getGraphics();
		 result.setColor(getRandColor(200, 250, random));    
		 result.fillRect( 0, 0, w, h);     
		 result.setFont(new Font("Times New Roman", Font.PLAIN, 18));     
		 result.setColor(new Color( 20, 20, 20));    
		 result.drawRect( 0, 0, w - 1, h - 1);      
		 result.setColor(getRandColor(160, 200, random));    
		 for (int i = 0; i < 60; i++) {      
			int x = random.nextInt(w);        
			int y = random.nextInt(h);        
			int xl = random.nextInt(12);        
			int yl = random.nextInt(12);        
			result.drawLine(x, y, x + xl, y + yl);    
		}
		 return result;
	}
	
	private static Color getRandColor(Random rnd){
		int r = 20 + rnd.nextInt(110);
		int g = 20 + rnd.nextInt(110);
		int b = 20 + rnd.nextInt(110);
		return new Color(r, g, b); //
	}
	
	private static Color getRandColor(int fc,int bc, Random random) {    
		 fc = fc > 255 ? 255 : fc;
		 bc = bc > 255 ? 255 : bc;
		 int r = fc + random.nextInt(bc - fc);    
		 int g = fc + random.nextInt(bc - fc);    
		 int b = fc + random.nextInt(bc - fc);    
		 return new Color(r, g, b); 
	}
}