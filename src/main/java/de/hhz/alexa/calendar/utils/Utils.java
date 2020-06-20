package de.hhz.alexa.calendar.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.Strings;

public class Utils {
	private static final String ONLINE = "online";
	public static final String REPROMT = "Was möchtest du noch wissen?";

	public static String parseDate(final Date date) {
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String dateString = sdf.format(date);
		return dateString.replace(dateString.substring(0, 4), "????");
	}

	public static String getLocation(String location) {
		if (Strings.isNullOrEmpty(location)) {
			return "";
		}
		if (location.toLowerCase().equals(ONLINE)) {
			return "online";
		} else {
			return "in " + location;
		}
	}

	public static String translateDate(String dateString) {
		Date now = new Date();

		String[] dateArray = dateString.split("-");
		if (dateArray.length == 3 && !dateArray[1].startsWith("W")) {
			int day = Integer.parseInt(dateArray[2]);
			if (day == now.getDate()) {
				return "heute";
			}
			if (day == now.getDate() + 1) {
				return "morgen";
			}

		} else if (dateArray.length == 2 && dateArray[1].startsWith("W")) {
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.setTime(now);
			if (mCalendar.get(Calendar.WEEK_OF_YEAR) == Integer.parseInt(dateArray[1].replaceAll("W", ""))) {
				return "diese Woche";
			}
			if (mCalendar.get(Calendar.WEEK_OF_YEAR) == Integer.parseInt(dateArray[1].replaceAll("W", ""))) {
				return "nächste Woche";
			}
		}
		return dateString;
	}
}
