package org.silver.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * 日期操作辅助类
 * 
 * @author Alvis
 * @version $Id: DateUtil.java, v 0.1 2014年3月28日 上午8:58:11 Alvis Exp $
 */
public final class DateUtil {
	private DateUtil() {
	}

	static String PATTERN = "yyyy-MM-dd";

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static final String format(Object date) {
		return format(date, PATTERN);
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static final String format(Object date, String pattern) {
		if (date == null) {
			return null;
		}
		if (pattern == null) {
			return format(date);
		}
		return new SimpleDateFormat(pattern).format(date);
	}

	/**
	 * 获取日期
	 * 
	 * @return
	 */
	public static final String getDate() {
		return format(new Date());
	}

	/**
	 * 获取日期时间
	 * 
	 * @return
	 */
	public static final String getDateTime() {
		return format(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 获取日期
	 * 
	 * @param pattern
	 * @return
	 */
	public static final String getDateTime(String pattern) {
		return format(new Date(), pattern);
	}

	/**
	 * 日期计算
	 * 
	 * @param date
	 * @param field
	 * @param amount
	 * @return
	 */
	public static final Date addDate(Date date, int field, int amount) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	/**
	 * 字符串转换为日期:不支持yyM[M]d[d]格式
	 * 
	 * @param date
	 * @return
	 */
	public static final Date stringToDate(String date) {
		if (date == null) {
			return null;
		}
		String separator = String.valueOf(date.charAt(4));
		String pattern = "yyyyMMdd";
		if (!separator.matches("\\d*")) {
			pattern = "yyyy" + separator + "MM" + separator + "dd";
			if (date.length() < 10) {
				pattern = "yyyy" + separator + "M" + separator + "d";
			}
		} else if (date.length() < 8) {
			pattern = "yyyyMd";
		}
		pattern += " HH:mm:ss.SSS";
		pattern = pattern.substring(0, Math.min(pattern.length(), date.length()));
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String getDate(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String strDate = sdf.format(date);
		return strDate;
	}

	/**
	 * 间隔天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static final Integer getDayBetween(Date startDate, Date endDate) {
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);
		end.set(Calendar.HOUR_OF_DAY, 0);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);

		long n = end.getTimeInMillis() - start.getTimeInMillis();
		return (int) (n / (60 * 60 * 24 * 1000l));
	}

	/**
	 * 间隔月
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static final Integer getMonthBetween(Date startDate, Date endDate) {
		if (startDate == null || endDate == null || !startDate.before(endDate)) {
			return null;
		}
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);
		int year1 = start.get(Calendar.YEAR);
		int year2 = end.get(Calendar.YEAR);
		int month1 = start.get(Calendar.MONTH);
		int month2 = end.get(Calendar.MONTH);
		int n = (year2 - year1) * 12;
		n = n + month2 - month1;
		return n;
	}

	/**
	 * 间隔月，多一天就多算一个月
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static final Integer getMonthBetweenWithDay(Date startDate, Date endDate) {
		if (startDate == null || endDate == null || !startDate.before(endDate)) {
			return null;
		}
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);
		int year1 = start.get(Calendar.YEAR);
		int year2 = end.get(Calendar.YEAR);
		int month1 = start.get(Calendar.MONTH);
		int month2 = end.get(Calendar.MONTH);
		int n = (year2 - year1) * 12;
		n = n + month2 - month1;
		int day1 = start.get(Calendar.DAY_OF_MONTH);
		int day2 = end.get(Calendar.DAY_OF_MONTH);
		if (day1 <= day2) {
			n++;
		}
		return n;
	}

	/**
	 * 按照指定的格式，将日期类型对象转换成字符串，例如：yyyy-MM-dd,yyyy/MM/dd,yyyy/MM/dd hh:mm:ss
	 * 如果传入的日期为null,则返回空值
	 * 
	 * @param date
	 *            日期类型对象
	 * @param format
	 *            需转换的格式
	 * @return String 日期格式字符串
	 */
	public static String formatDate(Date date, String format) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat formater = new SimpleDateFormat(format);
		return formater.format(date);
	}

	/**
	 * 将日期类型对象转换成yyyy-MM-dd类型字符串 如果传入的日期为null,则返回空值
	 * 
	 * @param date
	 *            日期类型对象
	 * @return String 日期格式字符串
	 */
	public static String formatDate(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		return formater.format(date);
	}

	/**
	 * 将日期类型对象转换成yyyy-MM-dd HH:mm:ss类型字符串 如果传入的日期为null,则返回空值
	 * 
	 * @param date
	 *            日期类型对象
	 * @return 日期格式字符串
	 */
	public static String formatTime(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formater.format(date);
	}

	/**
	 * 将字符串yyyy-MM-dd HH:mm:ss转换为yyyyMMddHHmmss 如果传入的字符串为null,则返回空值
	 * 
	 * @param dateTime
	 *            日期格式的字符串
	 * @return 日期格式字符串
	 */
	public static String toStringTime(String dateTime) {
		if (dateTime == null) {
			return "";
		}
		return dateTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");

	}

	/**
	 * 将字符串yyyyMMddhhmmss 转换成yyyy-MM-dd HH:mm:ss字符串格式 如果传入的字符串为null,则返回空值
	 * 
	 * @param date
	 *            字符串yyyyMMddhhmmss
	 * @return 日期格式字符串
	 */
	public static String toStringDate(String date) {
		if (date == null) {
			return "";
		}
		String reg = "(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})";
		date = date.replaceAll(reg, "$1-$2-$3 $4:$5:$6");
		return date;
	}

	/**
	 * 将字符串（yyyy-MM-dd）解析成日期
	 * 
	 * @param dateStr
	 *            日期格式的字符串
	 * @return 日期类型对象
	 */
	public static Date parseDate(String dateStr) {
		return parseDate(dateStr, "yyyy-MM-dd");
	}

	/**
	 * 将字符串（yyyy-MM-dd HH:mm:ss）解析成日期
	 * 
	 * @param dateStr
	 *            日期格式的字符串
	 * @return 日期类型对象
	 */
	public static Date parseDate2(String dateStr) {
		return parseDate(dateStr, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 按照指定的格式，将字符串解析成日期类型对象，例如：yyyy-MM-dd,yyyy/MM/dd,yyyy/MM/dd hh:mm:ss
	 * 
	 * @param dateStr
	 *            日期格式的字符串
	 * @param format
	 *            字符串的格式
	 * @return 日期类型对象
	 */
	public static Date parseDate(String dateStr, String format) {
		if (StringUtils.isEmpty(dateStr)) {
			return null;
		}
		SimpleDateFormat formater = new SimpleDateFormat(format);
		try {
			return formater.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将时间戳转换成yyyy-MM-dd HH:mm:ss字符串格式
	 * 
	 * @param timestamp
	 *            时间戳
	 * @return 日期格式字符串
	 */
	public static final String timestampParseTime(long timestamp) {
		if (timestamp < 0) {
			return null;
		}
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formater.format(timestamp);
	}

	/**
	 * 将时间戳转换为Date类型
	 * 
	 * @param timestamp
	 *            时间戳
	 * @return Date
	 */
	public static final Date timestampParseDate(long timestamp) {
		if (timestamp < 0) {
			return null;
		}
		String strTime = timestampParseTime(timestamp);
		return parseDate2(strTime);
	}

	/**
	 * 获取随机日期
	 * 
	 * @param beginDate
	 *            起始日期，格式为：yyyyMMdd
	 * @param endDate
	 *            结束日期，格式为：yyyyMMdd
	 * @return StringTime yyyyMMddHHddss
	 */

	public static String randomDate(Date beginDate, Date endDate) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHddss");
			// getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
			if (beginDate.getTime() >= endDate.getTime()) {
				return "";
			}
			long date = random(beginDate.getTime(), endDate.getTime());
			return format.format(new Date(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 生成随机日期
	 * 
	 * @param begin
	 * @param end
	 * @return
	 */
	private static long random(long begin, long end) {
		long rtn = begin + (long) (Math.random() * (end - begin));
		// 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
		if (rtn == begin || rtn == end) {
			return random(begin, end);
		}
		return rtn;
	}

	/**
	 * 随机生成一个3-5天前的日期
	 * 
	 * @return String日期,格式yyyyMMddHHddss
	 */
	public static final String randomCreateDate() {
		java.util.Random random = new java.util.Random();// 定义随机类
		int result = random.nextInt(5);// 返回[0,10)集合中的整数，注意不包括10
		Date dNow = new Date(); // 当前时间
		Calendar calendar2 = Calendar.getInstance(); // 得到日历
		calendar2.setTime(dNow);// 将当前时间赋给日历
		calendar2.add(Calendar.DATE, -2);
		dNow = calendar2.getTime();//
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.setTime(dNow);// 把当前时间赋给日历
		calendar.add(Calendar.DATE, -(result + 2));
		calendar.setTime(calendar.getTime());
		Date dBefore = calendar.getTime(); // 得到随机3-5天时间
		return randomDate(dBefore, dNow);
	}

	/**
	 * 根据订单日期,生成支付单日期,格式为订单日期之上随机增加5分钟与60秒
	 * @param orderDate 订单日期
	 * @return Date 支付单日期
	 */
	public static final Date randomPaymentDate(String orderDate) {
		java.util.Random random = new java.util.Random();// 定义随机类
		int minute = random.nextInt(5);// 返回[0,10)集合中的整数，注意不包括10
		int second = random.nextInt(60);
		Date oldDate = DateUtil.parseDate2(DateUtil.toStringDate(orderDate));
		Calendar nowTime = Calendar.getInstance();
		nowTime.setTime(oldDate);
		nowTime.add(Calendar.MINUTE, (minute + 1));
		nowTime.add(Calendar.SECOND, (second + 1));
		return nowTime.getTime();
	}
}
