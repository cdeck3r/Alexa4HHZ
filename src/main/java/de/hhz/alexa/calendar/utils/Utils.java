package de.hhz.alexa.calendar.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utils {
	private static final String ONLINE = "online";

	public static String parseDate(final Date date) {
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String dateString = sdf.format(date);
		return dateString.replace(dateString.substring(0, 4), "????");
	}

	public static String getEventAsString(List<HHZEvent> eventList, Date date) {
		StringBuilder mStringBuilder = new StringBuilder();

		if (eventList.size() == 0) {
			mStringBuilder.append("Es gibt keine Veranstaltung ???.");
			return mStringBuilder.toString();
		}
		mStringBuilder.append("Die Veranstaltung");

		eventList.forEach(element -> {
			mStringBuilder.append("Herr ");
			mStringBuilder.append(element.getTeacher());
			mStringBuilder.append(" ist am ");
			String dateString = parseDate(element.getStartTime());
			mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
			mStringBuilder.append(" um ");
			mStringBuilder.append(dateString.split(",")[1]);
			mStringBuilder.append(" ");
			mStringBuilder.append(element.getDescription());
			mStringBuilder.append(" ");
//			mStringBuilder.append("in ");
			mStringBuilder.append(element.getLocation());
			mStringBuilder.append(" . ");
		});

		return mStringBuilder.toString();
	}

	public static String getLocation(String location) {
		if (location.toLowerCase().equals(ONLINE)) {
			return "Die Veranstaltung is online";
		} else {
			return "in " + location;
		}
	}
}
