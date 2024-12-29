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

package cn.techarts.xkit.app.helper;

/**
 * @author rocwon@gmail.com
 */
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * It implements date-time functions based on JAVA new Date APIs. 
 */
public final class Time {
	public static final int DAY = 0;
	public static final int HOUR = 1;
	public static final int MINUTE = 2;
	public static final int SECOND = 3;
	public static final int MILLISECOND = 4;
	private static final long DAY_SECONDS = 86400000L;
	
	public static Date now() {
		return new Date();
	}
	
	/**
	 * @param hd High-Definition of time stamp dependent on your OS
	 *@return Returns a long number from 1970-0-01 00:00:00 
	 */
	public static long ts(boolean hd) {
		if(hd) {
			return System.nanoTime() / 1000000L;
		}else {
			return System.currentTimeMillis();
		}
	}
	
	public static String timestamp() {
		return String.valueOf(System.currentTimeMillis());
	}
	
	/***
	 * The begin of today (00:00:01)
	 */
	public static Date today() {
		return day(new Date(), true);
	}
	
	public static Date yesterday() {
		var instant = new Date().toInstant();
		long now = instant.getEpochSecond();
		return new Date((now - 86400) * 1000);
	}
	
	public static Date tomorrow() {
		var instant = new Date().toInstant();
		long now = instant.getEpochSecond();
		return new Date((now + 86400) * 1000);
	}
	
	public static int quarter() {
		return getQuarter(new Date());
	}
	
	public static int quarter(Date date) {
		return getQuarter(date);
	}
	
	/**
	 *@return Returns an array representing [year, month, day].<p>
	 *If you want to know the month number of now:<p>
	 * {@code int currentMonth = getDates(null)[1];}
	 */
	public static int[] getDates(Date date) {
		var d = date != null ? date : now();
		var localDate = getLocalDate(d);
		int year = localDate.getYear();
		int day = localDate.getDayOfMonth();
		int month = localDate.getMonthValue();
		return new int[] {year, month, day};
	}
	
	/**
	 *  Since 1970-01-01 to the given date
	 */
	public static int days(Date date) {
		if(date == null) return 0;
		return (int)toLocalDate(date).toEpochDay();
	}
	
	/**
	 *  Since 1970-01-01 to now
	 */
	public static int days() {
		return days(new Date());
	}
	
	/**
	 * Returns the minutes from 1970-01-01 0:0:0
	 */
	public static int minutes() {
		return (int)(System.currentTimeMillis() / 60000);
	}
	
	/**
	 * Returns the minutes from 1970-01-01 0:0:0 to the specified date
	 */
	public static int minutes(Date date) {
		if(date == null) return minutes();
		return (int)(date.getTime() / 60000);
	}
	
	/**
	 * Returns the seconds from 1970-01-01 0:0:0
	 */
	public static long seconds() {
		return System.currentTimeMillis() / 1000;
	}
	
	/**
	 * Returns the seconds from 1970-01-01 0:0:0
	 */
	public static long seconds(Date date) {
		if(date == null) return seconds();
		return (long)(date.getTime() / 1000);
	}
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param unit Defined in this class: Time.DAY | HOUR | MINUTE | SECOND | MILLISECOND
	 */
	public static void sleep(int timeout, int unit) {
		try {
			switch(unit) {
				case DAY:
					TimeUnit.DAYS.sleep(timeout);
					break;
				case HOUR:
					TimeUnit.HOURS.sleep(timeout);
					break;
				case MINUTE:
					TimeUnit.MINUTES.sleep(timeout);
					break;
				case SECOND:
					TimeUnit.SECONDS.sleep(timeout);
					break;
				case MILLISECOND:
					TimeUnit.MILLISECONDS.sleep(timeout);
					break;
				default:
					TimeUnit.MILLISECONDS.sleep(timeout);
			}			
		}catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param date The pattern is "yyyy-MM-dd" or "yyyy/MM/dd HH:mm:ss"
	 */
	public static Date parse(String date) {
		if(Empty.is(date)) return null;
		DateTimeFormatter formatter = null;
		var source = date.replace('/', '-');
		if(source.length() == 10) {
			formatter = DateTimeFormatter.ISO_LOCAL_DATE;
			return toDate(LocalDate.parse(source, formatter));
		}else { //Pattern: yyyy-MM-dd HH:mm:ss
			source = source.replace(' ', 'T');
			formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			return toDate(LocalDateTime.parse(source, formatter));
		}
	}
	
	public static Date toDate(int year, int month, int day) {
		return toDate(LocalDate.of(year, month, day));
	}
	
	/**
	 * Convert an integer(days from 1970-01-01) to a java Date object without hh:MM:ss
	 * @param days If days is less than (or equals) 0, return the date of now.
	 */
	public static Date toDate(int days) {
		if(days <= 0) return new Date();
		return new Date(days * DAY_SECONDS - 1000);
	}
	
	public static Date toDate(long ms) {
		if(ms <= 0) return new Date();
		return new Date(ms);
	}
	
	public static Date toDate(LocalDate localDate) {
		if(localDate == null) return null;
		var zoneId = ZoneId.systemDefault();
		var zdt = localDate.atStartOfDay(zoneId);
		return Date.from(zdt.toInstant());
	}
	
	public static Date toDate(LocalDateTime localDateTime) {
		if(localDateTime == null) return null;
		var zoneId = ZoneId.systemDefault();
		var instant = localDateTime.atZone(zoneId).toInstant();
		return Date.from(instant);
	}
	
	public static LocalDate toLocalDate(Date date) {
		if(date == null) return null;
		var instant = date.toInstant();
		var zoneId = ZoneId.systemDefault();
		return instant.atZone(zoneId).toLocalDate();
	}
	
	public static LocalDateTime toLocalDateTime(Date date) {
		if(date == null) return null;
		var instant = date.toInstant();
		var zoneId = ZoneId.systemDefault();
		return instant.atZone(zoneId).toLocalDateTime();
	}
	
	/**
	 * @return yyyy-MM-dd (HH:mm:ss)
	 */
	public static String format(Date date, boolean withTime) {
		if(date == null) return null;
		if(!withTime) {
			var localDate = toLocalDate(date);
			var formatter = DateTimeFormatter.ISO_LOCAL_DATE;
			return localDate.format(formatter);
		}else {
			var localDateTime = toLocalDateTime(date);
			var formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			var result = localDateTime.format(formatter);
			return result.substring(0, 19).replace('T', ' ');
		}
	}
	
	/**
	 * @return yyyy-MM-dd HH:mm:ss of now
	 */
	public static String format() {
		return format(new Date(), true);
	}
	
	/**
	 *@param withSeconds 
	 * If you need a date string without seconds (the last 3 chars), 
	 * please pass a false to this parameter.
	 */
	public static String format(boolean withSeconds, Date date) {
		int end = withSeconds ? 19 : 16;
		var time = date != null ? date : new Date();
		var localDateTime = toLocalDateTime(time);
		var formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		var result = localDateTime.format(formatter);
		return result.substring(0, end).replace('T', ' ');
	}
	
	/**
	 * Returns an BASIC_ISO_DATE format integer that looks like 20190909, 20200101
	 */
	public static int format(Date date) {
		if(date == null) return 0;
		var localDate = toLocalDate(date);
		int year = localDate.getYear();
		int day = localDate.getDayOfMonth();
		int month = localDate.getMonthValue();
		return year * 10000 + month * 100 + day;
	}
	
	/**
	 * Returns the beginning of the day(yyyy-MM-dd 00:00:01)
	 */
	public static Date beginOf(Date date) {
		return day(date, true);
	}
	
	/**
	 * Returns the end of the day(yyyy-MM-dd 2300:59:59)
	 */
	public static Date endOf(Date date) {
		return day(date, false);
	}
	
	/**
	 * Returns the beginning of the today(yyyy-MM-dd 00:00:01)
	 */
	public static Date beginOfToday() {
		return day(new Date(), true);
	}
	
	/**
	 * Returns the end of the today(yyyy-MM-dd 2300:59:59)
	 */
	public static Date endOfToday() {
		return day(new Date(), false);
	}
	
	/**
	 * Returns a date representing the 1st of the specific year and month
	 */
	public static Date beginOfMonth(int year, int month) {
		if(year < 1970 || month < 1 || month > 12) return null;
		return parse(year + (month < 10 ? "-0" + month : "-" + month) + "-01");
	}
	
	/**
	 * Returns a date representing the 30th or 31th of the specific year and month
	 */
	public static Date endOfMonth(int year, int month) {
		if(year < 1970 || month < 1 || month > 12) return null;
		int day = actualMaxDays(year, month);
		var m = (month < 10 ? "-0" + month : "-" + month);
		return parse(year +  m + "-" + day + " 23:59:59");
	}
	
	public static Date beginOfMonth(Date date) {
		var localDate = getLocalDate(date);
		var dom = localDate.getDayOfMonth();
		return toDate(localDate.plusDays(1 - dom));
	}
	
	public static Date endOfMnoth(Date date) {
		var localDate = getLocalDate(date);
		var dom = localDate.getDayOfMonth();
		var bgn = localDate.plusDays(1 - dom);
		return toDate(bgn.plusMonths(1).plusDays(-1));
	}
	
	public static Date beginOfYear() {
		return year(-1, true);
	}
	
	public static Date endOfYear() {
		return year(-1, false);
	}
	
	public static Date beginOfYear(int year) {
		return year(year, true);
	}
	
	public static Date endOfYear(int year) {
		return year(year, false);
	}
	
	/**
	 * Set the @param date with the specific hour, minute and second
	 */
	public static Date setTime(Date date, int hour, int minute, int second) {
		var localDate = getLocalDateTime(date);
		int h = localDate.getHour();
		int m = localDate.getMinute();
		int s = localDate.getSecond();
		localDate.plusHours(hour - h);
		localDate.plusMinutes(minute - m);
		localDate.plusSeconds(second - s);
		return toDate(localDate.plusSeconds(second - s));
	}
	
	/**
	 * Returns the begin or end time of the day
	 */
	private static Date day(Date date, boolean begin) {
		var localDate = getLocalDateTime(date);
		int hnow = localDate.getHour();
		int mnow = localDate.getMinute();
		int h = begin ? -hnow : 23 - hnow;
		int m = begin ? -mnow : 59 - mnow;
		return toDate(localDate.plusHours(h).plusMinutes(m));
	}
	
	/**
	 * Returns the begin and end date of the current year.
	 */
	private static Date year(int year, boolean begin) {
		var localDate = LocalDate.now();
		var y = year < 1970 ? localDate.getYear() : year;
		if(begin) {
			return toDate(LocalDateTime.of(y, 1, 1, 0, 0, 1));
		}else {
			return toDate(LocalDateTime.of(y, 12, 31, 23, 59));
		}
	}
	
	private static LocalDate getLocalDate(Date date) {
		if(date == null) return LocalDate.now();
		return toLocalDate(date);
	}
	
	private static LocalDateTime getLocalDateTime(Date date) {
		if(date == null) return LocalDateTime.now();
		return toLocalDateTime(date);
	}
	
	public static Date nextYears(Date date, int years) {
		var localDate = getLocalDate(date);
		return toDate(localDate.plusYears(years));
	}
	
	public static Date nextMonths(Date date, int months) {
		var localDate = getLocalDate(date);
		return toDate(localDate.plusMonths(months));
	}
	
	public static Date nextDays(Date date, int days) {
		var localDate = getLocalDate(date);
		return toDate(localDate.plusDays(days));
	}
	
	public static boolean isLeapYear(Date arg) {
		var date = arg != null ? arg : new Date();
		var localDate = toLocalDate(date);
		return localDate.getYear() % 4 == 0;
	}
	
	public static boolean isLeapYear() {
		return isLeapYear(new Date());
	}
	
	public static boolean isSameDay(Date arg0, Date arg1) {
		if(arg0 == null || arg1 == null) return false;
		var days0 = Time.format(arg0);
		return days0 == Time.format(arg1);
	}
	
	public static boolean isSameMonth(Date date1, Date date2) {
		if(date1 == null || date2 == null) return false;
		var local1 = toLocalDate(date1);
		var local2 = toLocalDate(date2);
		if(local1.getYear() != local2.getYear()) return false;
		return local1.getMonthValue() == local2.getMonthValue();
	}
	
	public static boolean isSameYear(Date arg0, Date arg1) {
		if(arg0 == null || arg1 == null) return false;
		return getLocalDate(arg0).getYear() == getLocalDate(arg1).getYear();
	}
	
	/**
	 *@return Returns true if the specified date (null is today) is the last day of the month 
	 */
	public static boolean isMonthLastDay(Date date) {
		var d = date == null ? new Date() : date;
		var localDate = Time.toLocalDate(d);
		var lastDay = localDate.lengthOfMonth();
		return localDate.getDayOfMonth() == lastDay;
	}
	
	/**
	 * If the day of week is NOT 6 or 7, the TRUE returned.
	 */
	public static boolean isWorkDay(Date date) {
		if(date == null) return false;
		var localDate = getLocalDate(date);
		int weekday = localDate.getDayOfWeek().getValue();
		return weekday > 0 && weekday < 6;
	}
	
	/**
	 * If the day of week is 6 or 7, the TRUE returned.
	 */
	public static boolean isWeekend(Date date) {
		var localDate = getLocalDate(date);
		int weekday = localDate.getDayOfWeek().getValue();
		return weekday == 6 || weekday == 7;
	}
	
	public static float spanOfHours(Date end, Date bgn){
		if(end == null || bgn == null) return 0f;
		long ms = end.getTime() - bgn.getTime();
		return ms <= 0 ? 0f : (float)(ms / 3600000f);
	}
	
	public static int spanOfMinutes(Date end, Date bgn) {
		if(end == null || bgn == null) return 0;
		long ms = end.getTime() - bgn.getTime();
		return ms <= 0 ? 0 : (int)(ms / 60000);
	}
	
	/**
	 * Returns the duration between the specific 2 dates
	 */
	public static int spanOfDays(Date end, Date bgn){
		if(end == null || bgn == null) return 0;
		long ms = end.getTime() - bgn.getTime();
		return ms <= 0 ? 0 : (int)(ms / 86400000);
	}
	
	/**
	 * Returns true if the @param date after NOW<br>
	 * The method follows MAX-RIGHT rule. Namely, tries its best to return TRUE.
	 * @param date The method changes the date to 23:59:59 of the given day.<br>
	 * e.g. You pass 2019-11-27 16:12:30, actually it's 2019-11-27 23:59:59
	 */
	public static boolean after(Date date) {
		if(date == null) return false;
		var thatDay = endOf(date);
		return thatDay.getTime() > new Date().getTime();
	}
	
	/**
	 * Another version of the method {@link after}.
	 * Returns true if the @param date after NOW. If it is NULL, returns true yet.
	 * */
	public static boolean after2(Date date){
		if( date == null) return true;
		var target = endOf(date);
		return target.getTime() > new Date().getTime();
	}
	
	/**
	 * @param begin The begin date of the period
	 * @param end the end date of the period
	 * @param actual The date you want to compare
	 * @return Returns true if the actual is between the begin and end
	 */
	public static boolean between(Date begin, Date end, Date actual) {
		var due2 = Time.endOf(end);
		var b = begin == null ? 0 : begin.getTime();
		var d = end == null ? 2552313600000L : due2.getTime();
		if(b == 0 && d == 0) return true;
		var a = actual == null ? new Date().getTime() : actual.getTime();
		return a > b && a < d;
	}
	
	/**
	 * @return date1 < date2: -1,<br>
	 * 		   date1 = date2: 0, <br>
	 * 		   date1 > date2: 1
	 * */
	public static int compare(Date date1, Date date2) {
		if(date1 == null) return -1;
		if(date2 == null) return 1;
		long bgn = date1.getTime();
		long due = date2.getTime();
		if(bgn == due) return 0;
		return bgn < due ? -1 : 1;
	}
	
	/**
	 * Check now is whether out of the specified deadline date.<p>
	 * @return true: now < deadline; false: now < deadline
	 */
	public static boolean expired(Date deadline){
		if( deadline == null) return false;
		return new Date().getTime() > endOf(deadline).getTime();
	}
	
	/**
	 * Check the specified parameter date is whether out of the given deadline date.<p>
	 * @return true: date < deadline; false: date > deadline
	 */
	public static boolean expired(Date date, Date deadline) {
		if(date == null || deadline == null) return false;
		return date.getTime() > endOf(deadline).getTime();
	}
	
	public static int monthMaxDays(int year, int month) {
		return actualMaxDays(year, month);
	}
	
	public static int monthMaxDays(Date date) {
		var localDate = toLocalDate(date);
		var year = localDate.getYear();
		var month = localDate.getMonthValue();
		return actualMaxDays(year, month);
	}
	
	public static int monthMaxDays() {
		var localDate = toLocalDate(new Date());
		var year = localDate.getYear();
		var month = localDate.getMonthValue();
		return actualMaxDays(year, month);
	}
	
	private static int actualMaxDays(int year, int month) {
		switch(month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				return 31;
			case 4:
			case 6:
			case 9:
			case 11:
				return 30;
			case 2:
				return year % 4 == 0 ? 29 : 28;
			default:
				return 0;
		}
	}
	
	private static int getQuarter(Date date) {
		var localDate = getLocalDate(date);
		int month = localDate.getMonthValue();
		int q = month < 4 ? 1 : month > 9 ? 4 : 0;
		return q > 0 ? q : month < 7 ? 2 : 3;
	}
	
	/**
	 *@return The cycles of specific duration since UTC 1970-01-01 00:00:00 
	 */
	public static long getTimeCycles(int duration) {
		var d = duration <= 0 ? 32 : duration;
		var seconds = System.currentTimeMillis() / 1000;
		return d == 32 ? seconds >> 5 : (long)(seconds / d);
	}
}