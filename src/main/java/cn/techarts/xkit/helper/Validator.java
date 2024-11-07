package cn.techarts.xkit.helper;

import java.util.regex.Pattern;

/**
 * The class contains series validate* methods to validate kinds of numbers 
 */
public final class Validator {
	private static final String CODES = "0123456789ABCDEFGHJKLMNPQRTUWXY";
	private static final int[] WEIGHTS1 = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
	private static final int[] WEIGHTS2 = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};
	private static final String MP_PATTERN = "^0?(13[0-9]|15[012356789]|17[013678]|18[0-9]|14[57])[0-9]{8}$";
	/**
	 * GB11643-1999: Citizen Identification Number
	 */
	public static boolean validateCIN(String code) {
		if(code == null || code.length() != 18) return false;
		int sum = 0;
		var chars = code.toCharArray();
		for(var i = 0; i < 17; i++) {
			sum += (chars[i] - 48) * WEIGHTS1[i];
		}
		return chars[17] == "10X98765432".charAt(sum % 11);
	}
	
	/**
	 * GB-32100-2015: Unified-Social-Credit-Identifier
	 */
	public static boolean validateUSCI(String code) {
		if(code == null || code.length() != 18) return false;
		int sum = 0;
		var chars = code.toUpperCase().toCharArray();
		for(var i = 0; i < 17; i++) {
			sum += CODES.indexOf(chars[i]) * WEIGHTS2[i];
		}
		return chars[17] == CODES.charAt(31 - sum % 31);
	}
	
	public static boolean validateMobile(String number) {
		if(number == null) return false;
		var mobile = number.trim();
		if(mobile.length() != 11) return false;
		var p = Pattern.compile(MP_PATTERN);
		return p.matcher(mobile).matches();
	}
	
	/**
	 * ITU-T Recommendation E.118: ICCID (Based on The algorithm Luhn)
	 */
	public static boolean validateICCID(String iccid) {
		if(iccid == null || iccid.length() != 20) return false;
		int size = iccid.length();
		int sumOfDigitals = 0;
		boolean isEvenIndex = true;
		char[] chars = iccid.toCharArray();
		for(int i = size - 2; i >= 0; i--) {
			int digital = chars[i] - 48;
			if(isEvenIndex) {
				digital *= 2;
				if(digital > 9) digital -= 9;
			}
			sumOfDigitals += digital;
			isEvenIndex = !isEvenIndex;
		}
		int last = 10 - sumOfDigitals % 10;
		return iccid.charAt(19) - 48 == (last < 10 ? last : 0);
	}
}
