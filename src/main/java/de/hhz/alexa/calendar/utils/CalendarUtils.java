package de.hhz.alexa.calendar.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import de.hhz.alexa.calendar.datasource.DataSourceFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.auth.Credentials;

@SuppressWarnings("deprecation")
public class CalendarUtils {
	private static final String APPLICATION_NAME = "Alexa4HHZ";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String HHZ_CALENDAR = "9fbtqhp79a3oqvq6v0ur434j2s@group.calendar.google.com";
	private static final String CANCELLED = "cancelled";
	private static final String DBE_PREFIX = "DBE";
	private static final String SCM_PREFIX = "SCM";
	private Credential accesTocken;
	List<HHZEvent> eventList = new ArrayList<HHZEvent>();
	List<HHZEvent> modifiedEvents = new ArrayList<HHZEvent>();

	public CalendarUtils(final Credential acessTocken) {
		this.accesTocken = acessTocken;
	}

	public List<HHZEvent> listEvents() throws Exception {

		if (this.eventList.size() > 0) {
			return this.eventList;
		}
		eventList = new ArrayList<HHZEvent>();
		ArrayList<HHZEvent> eventsToAdd = new ArrayList<HHZEvent>();

		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = null;
		HashMap<String, String> savedEvents = DataSourceFactory.getInstance().loadEvents();
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

//		GoogleCredential credential = new GoogleCredential().setAccessToken(this.accesTocken);
		Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.accesTocken)
				.setApplicationName(APPLICATION_NAME).build();
		events = service.events().list("primary").setTimeMin(now).
//				setCalendarId(HHZ_CALENDAR).
				setOrderBy("startTime")
				.setSingleEvents(true).execute();
		List<Event> items = events.getItems();
		if (!items.isEmpty()) {
			for (Event event : items) {
				HHZEvent course = this.createEvent(event);
				if (course != null) {

					this.eventList.add(course);
					if (!savedEvents.containsKey(event.getId())) {
						eventsToAdd.add(course);
					}
				}

			}
			DataSourceFactory.getInstance().saveEvents(eventsToAdd);
		}
		return this.eventList;
	}

	public List<HHZEvent> listModifiedEvents() throws Exception {

		if (this.modifiedEvents.size() > 0) {
			return modifiedEvents;
		}
		HashMap<String, String> ids = DataSourceFactory.getInstance().loadEvents();
		if (ids.size() <= 0) {
			return new ArrayList<HHZEvent>();
		}
		int limit = ids.size() > 5 ? 5 : ids.size();
		int counter = 0;
		modifiedEvents = new ArrayList<HHZEvent>();
		Event event = null;
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//		GoogleCredential credential = new GoogleCredential().setAccessToken(this.accesTocken);
		Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, accesTocken)
				.setApplicationName(APPLICATION_NAME).build();
		for (String eventId : ids.keySet()) {
			counter++;
			if (counter >= limit) {
				break;
			}
			Calendar.Events.Get getRequest = service.events().get("primary", eventId);
			getRequest.setRequestHeaders(new HttpHeaders().setIfNoneMatch(ids.get(eventId)));

			try {
				event = getRequest.execute();
			} catch (GoogleJsonResponseException e) {
				continue;
			}
			HHZEvent course = this.createEvent(event);
			if (course != null) {
				modifiedEvents.add(course);
				DataSourceFactory.getInstance().updateEvent(course);
			}
		}
		return modifiedEvents;
	}

	private String[] getStringArray(String summary) {
		summary = summary.replaceAll("null", "");
		summary = summary.replaceAll("[:,\\(]", "-");
		summary = summary.replaceAll("\\)", "");
		summary = summary.replaceFirst(" ", "-");
		summary = summary.replaceAll("--", "-");
		return summary.split("-");
	}

	private HHZEvent createEvent(Event event) {

		if (event.getSummary().startsWith(SCM_PREFIX)) {
			return null;
		}
		String[] summaryArray = this.getStringArray(event.getSummary());
		String description = null;
		String type = null;
		String semester = "";
		String teacher = null;
		boolean isCourse = false;
		DateTime start = event.getStart().getDateTime() != null ? event.getStart().getDateTime()
				: event.getStart().getDate();

		if (summaryArray.length >= 3) {
			isCourse = true;
			teacher = summaryArray[2];
			description = summaryArray[1];
			String[] semesterArray = summaryArray[0].replaceAll(DBE_PREFIX, "").split("/");
			for (int i = 0; i < semesterArray.length; i++) {
				semester += semesterArray[i].substring(0, 1);
			}
			description = summaryArray[1];
			teacher = summaryArray[2];
			if (summaryArray.length - 1 > 2) {
				type = summaryArray[3];
			}
		} else {
			description = summaryArray[0];
		}
		HHZEvent course = new HHZEvent(description, teacher, semester, new Date(start.getValue()), event.getLocation(),
				event.getId(), event.getId());
		course.setCourse(isCourse);
		course.setCancelled(event.getStatus() == CANCELLED);
		course.setType(type);

		return course;
	}
}
