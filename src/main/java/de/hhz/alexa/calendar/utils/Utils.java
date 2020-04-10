package de.hhz.alexa.calendar.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
	public static String parseDate(final Date date) {
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String dateString = sdf.format(date);
		return dateString.replace(dateString.substring(0, 4), "????");
	}
}
