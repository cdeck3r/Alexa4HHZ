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
import com.google.api.client.util.Strings;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import de.hhz.alexa.calendar.datasource.DataSourceFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.auth.Credentials;

@SuppressWarnings("deprecation")
public class CalendarUtils {
	private static final String APPLICATION_NAME = "Alexa4HHZ";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String HHZ_CALENDAR = "9fbtqhp79a3oqvq6v0ur434j2s@group.calendar.google.com";
	private static final String CANCELLED = "cancelled";
	private static final String DBE_PREFIX = "DBE";
	private static final String SCM_PREFIX = "SCM";
	private Credential credential;
	private String accessTocken;
	List<HHZEvent> eventList = new ArrayList<HHZEvent>();
	List<HHZEvent> modifiedEvents = new ArrayList<HHZEvent>();

	public CalendarUtils(final Credential acessTocken) {
		this.credential = acessTocken;
	}

	public CalendarUtils(final String acessTocken) {
		this.accessTocken = acessTocken;
	}

	public List<HHZEvent> listEvents() throws Exception {

		if (this.eventList.size() > 0) {
			return this.eventList;
		}
		eventList = new ArrayList<HHZEvent>();
		ArrayList<HHZEvent> eventsToAdd = new ArrayList<HHZEvent>();

		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = null;
		List<HHZEvent> savedEvents = DataSourceFactory.getInstance().loadEvents();
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Calendar service;
		if (!Strings.isNullOrEmpty(this.accessTocken)) {
			GoogleCredential mGoogleCredential = new GoogleCredential().setAccessToken(this.accessTocken);
			service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, mGoogleCredential)
					.setApplicationName(APPLICATION_NAME).build();
		} else {
			service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.credential)
					.setApplicationName(APPLICATION_NAME).build();
		}
		events = service.events().list("primary").setTimeMin(now).
				setCalendarId(HHZ_CALENDAR).
				setOrderBy("startTime").setSingleEvents(true).execute();
		List<Event> items = events.getItems();
		if (!items.isEmpty()) {
			for (Event event : items) {
				
				HHZEvent course = this.createEvent(event);
				if (course != null) {

					this.eventList.add(course);
					List<HHZEvent> uniqueEvent = savedEvents.stream()
							.filter(element -> element.getId().equals(event.getId())).collect(Collectors.toList());
					if (uniqueEvent == null || uniqueEvent.size() <= 0) {
						// event not saved in database
						eventsToAdd.add(course);
					} else {
						if (uniqueEvent.get(0).getStartTime().before(new Date())) {
							DataSourceFactory.getInstance().deleteEvent(uniqueEvent.get(0));
						}
					}
				}

			}
			DataSourceFactory.getInstance().saveEvents(eventsToAdd);
		}
		return this.eventList;
	}

	public List<HHZEvent> listModifiedEvents() throws Exception {
		modifiedEvents = new ArrayList<HHZEvent>();

		List<HHZEvent> ids = DataSourceFactory.getInstance().loadEvents();
		if (ids.size() <= 0) {
			return modifiedEvents;
		}
		int limit = ids.size() > 10 ? 10 : ids.size();
		int counter = 0;
		Event event = null;
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Calendar service;
		if (!Strings.isNullOrEmpty(accessTocken)) {
			GoogleCredential mGoogleCredential = new GoogleCredential().setAccessToken(this.accessTocken);
			service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, mGoogleCredential)
					.setApplicationName(APPLICATION_NAME).build();
		} else {
			service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.credential)
					.setApplicationName(APPLICATION_NAME).build();
		}

		for (HHZEvent ev : ids) {
			if (counter >= limit) {
				break;
			}
			counter++;
			Calendar.Events.Get getRequest = service.events().get("primary", ev.getId());
			getRequest.setRequestHeaders(new HttpHeaders().setIfNoneMatch(ev.geteTag()));
			try {
				event = getRequest.execute();

			} catch (GoogleJsonResponseException e) {
				continue;
			}
			HHZEvent course = this.createEvent(event);
			if (course != null) {
				if (!course.isCancelled() && course.getStartTime().after(ev.getStartTime())) {
					course.setPosponed(true);
				}
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
		} else if(event.getSummary().startsWith(DBE_PREFIX)) {
			description = summaryArray[1];
		} else {
			description = summaryArray[0];
		}
		HHZEvent course = new HHZEvent(description, teacher, semester, new Date(start.getValue()), event.getLocation(),
				event.getId(), event.getEtag());
		course.setCourse(isCourse);
		course.setCancelled(event.getStatus().equals(CANCELLED));
		course.setType(type);

		return course;
	}
}
