package de.hhz.alexa.calendar.calender;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

@SuppressWarnings("deprecation")
public class CalendarUtils {
	private static final String APPLICATION_NAME = "Alexa4HHZ";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String HHZ_CALENDAR = "9fbtqhp79a3oqvq6v0ur434j2s@group.calendar.google.com";
	private static final String CANCELLED = "cancelled";
	private static final String DBE_PREFIX = "DBE";
	private static final String SCM_PREFIX = "SCM";
	private static final int MAX_DB_EVENT = 10;
	private String accessTocken;
	private String user;
	List<HHZEvent> eventList = new ArrayList<HHZEvent>();
	List<HHZEvent> modifiedEvents = new ArrayList<HHZEvent>();

	public CalendarUtils(final String acessTocken) throws Exception {
		this.accessTocken = acessTocken;
		this.user = this.getEmailAddress();
	}

	public List<HHZEvent> listEvents() throws Exception {

		if (this.eventList.size() > 0) {
			return this.eventList;
		}
		eventList = new ArrayList<HHZEvent>();
		ArrayList<HHZEvent> eventsToAdd = new ArrayList<HHZEvent>();

		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = null;
		List<HHZEvent> savedEvents = DataSourceFactory.getInstance().loadEvents(0, this.user);
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		GoogleCredential mGoogleCredential = new GoogleCredential().setAccessToken(this.accessTocken);
		Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, mGoogleCredential)
				.setApplicationName(APPLICATION_NAME).build();

		events = service.events().list("primary").setTimeMin(now).setCalendarId(HHZ_CALENDAR).setOrderBy("startTime")
				.setSingleEvents(true).execute();
		List<Event> items = events.getItems();
		if (!items.isEmpty()) {
			for (Event event : items) {

				HHZEvent course = this.createEvent(event);

				if ((course != null) && (course.getDescription() != null)) {
					this.eventList.add(course);
					List<HHZEvent> uniqueEvent = savedEvents.stream()
							.filter(element -> element.getId().equals(event.getId())).collect(Collectors.toList());
					if (uniqueEvent == null || uniqueEvent.size() <= 0) {
						// add event to database
						if ((eventsToAdd.size() + savedEvents.size()) < MAX_DB_EVENT) {
							eventsToAdd.add(course);
						}
					} else {
						// Delete old events from database
						if (uniqueEvent.get(0).getStartTime().before(new Date())) {
							DataSourceFactory.getInstance().deleteEvent(uniqueEvent.get(0), this.user);
						}
					}
				}

			}
			DataSourceFactory.getInstance().saveEvents(eventsToAdd, this.user);
		}
		return this.eventList;
	}

	public List<HHZEvent> listModifiedEvents() throws Exception {
		modifiedEvents = new ArrayList<HHZEvent>();
		List<HHZEvent> ids = DataSourceFactory.getInstance().loadEvents(0, this.user);
		if (ids.size() <= 0) {
			return modifiedEvents;
		}

		int limit = ids.size() > MAX_DB_EVENT ? MAX_DB_EVENT : ids.size();
		int counter = 0;
		Event event = null;
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential mGoogleCredential = new GoogleCredential().setAccessToken(this.accessTocken);
		Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, mGoogleCredential)
				.setApplicationName(APPLICATION_NAME).build();

		for (HHZEvent ev : ids) {
			if (counter >= limit) {
				break;
			}
			if (ev.getStartTime().before(new Date())) {
				DataSourceFactory.getInstance().deleteEvent(ev, this.user);
				continue;
			}
			counter++;
			Calendar.Events.Get getRequest = service.events().get("primary", ev.getId()).setCalendarId(HHZ_CALENDAR);
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
				DataSourceFactory.getInstance().updateEvent(course, this.user);
			}
		}
		return modifiedEvents;
	}

	private String[] getStringArray(String summary) {
		summary = summary.replaceAll("null", "");
		if (summary.startsWith(DBE_PREFIX)) {
			summary = summary.replaceAll("[:,\\(]", "-");
			summary = summary.replaceAll("\\)", "");
			summary = summary.replaceFirst(" ", "-");
			summary = summary.replaceAll("--", "-");
		}
		return summary.split("-");
	}

	public String getEmailAddress() throws Exception {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential mGoogleCredential = new GoogleCredential().setAccessToken(this.accessTocken);
		Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, mGoogleCredential)
				.setApplicationName(APPLICATION_NAME).build();
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = service.events().list("primary").setTimeMin(now).setMaxResults(1).setSingleEvents(true)
				.execute();
		return events.getSummary();
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
		} else if (event.getSummary().startsWith(DBE_PREFIX)) {
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
