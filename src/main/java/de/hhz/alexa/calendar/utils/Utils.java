package de.hhz.alexa.calendar.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.util.Strings;

public class Utils {
	private static final String ONLINE = "online";

	public static String parseDate(final Date date) {
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String dateString = sdf.format(date);
		return dateString.replace(dateString.substring(0, 4), "????");
	}

	public static String getLocation(String location) {
		if(Strings.isNullOrEmpty(location)) {
			return "";
		}
		if (location.toLowerCase().equals(ONLINE)) {
			return "online";
		} else {
			return "in " + location;
		}
	}
}
