package de.hhz.alexa.calendar.utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class CalendarUtils {
	private static final String APPLICATION_NAME = "Alexa4HHZ";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String VALUE_PATTERN = "([a-zA-Z0-9]*)(: )([a-zA-Z0-9]* [a-zA-Z0-9]*) ([(a-zA-Z0-9. )]*)";
	private Pattern mPattern = Pattern.compile(VALUE_PATTERN);
	private static final String HHZ_CALENDAR = "9fbtqhp79a3oqvq6v0ur434j2s@group.calendar.google.com";
	private String accesTocken;

	public CalendarUtils(final String acessTocken) {
		this.accesTocken = acessTocken;
	}

	public List<Course> listEvents() throws GeneralSecurityException, IOException {

		List<Course> eventList = new ArrayList<Course>();
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = null;
	
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		
			GoogleCredential credential = new GoogleCredential().setAccessToken(this.accesTocken);
			Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();
			events = service.events().list("primary").setMaxResults(10).setTimeMin(now).setCalendarId(HHZ_CALENDAR)
					.setOrderBy("startTime").setSingleEvents(true).execute();
		List<Event> items = events.getItems();
		if (!items.isEmpty()) {
			for (Event event : items) {
				Matcher mMatcher = mPattern.matcher(event.getSummary());
				if (mMatcher.find()) {
					eventList.add(new Course(mMatcher.group(3), mMatcher.group(4).replaceAll("[(,)]", ""),
							mMatcher.group(1),new Date(event.getStart().getDateTime().getValue()), event.getLocation()));
				}
			}

		}
		return eventList;
	}
}
