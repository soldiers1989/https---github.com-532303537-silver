package org.silver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码校验工具类
 * 
 * @since 2018-04-26
 */
public class PhoneUtils {

	/**
	 * <li>中国电信号段 133、149、153、173、177、180、181、189、199</li> 
	 * <li>中国联通号段 130、131、132、145、155、156、166、175、176、185、186 </li> 
	 * <li>中国移动号段 134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188、198</li> 
	 * <li>其他号段 14号段以前为上网卡专属号段，如中国联通的是145，中国移动的是147等等。 </li> 
	 * <li>虚拟运营商 电信：1700、1701、1702</li> 
	 * <li>移动：1703、1705、1706 </li> 
	 * <li>联通：1704、1707、1708、1709、171</li>  
	 * <li>卫星通信：1349</li> 
	 * @param 手机号码
	 * @return boolean 布尔值
	 */
	public static boolean isPhone(String phone) {
		String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(16([6]))|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
		if (StringEmptyUtils.isEmpty(phone) || phone.length() != 11) {
			return false;
		} else {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(phone);
			boolean isMatch = m.matches();
			if (!isMatch) {
				return false;
			}
			return isMatch;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(isPhone("13558810388"));
	}
}
