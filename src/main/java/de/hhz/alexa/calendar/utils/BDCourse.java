package de.hhz.alexa.calendar.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.util.Strings;

public class BDCourse {
	private static final String KW = "W";
//	private static BDEvent mBdEvent;
	private CalendarUtils mCalendarUtils;

	public BDCourse(final String accessTocken) {
		this.mCalendarUtils = new CalendarUtils(accessTocken);
	}

	public List<Course> listSubjects() throws GeneralSecurityException, IOException {

		return this.mCalendarUtils.listEvents();
	}

	public List<Course> listSubjects(final String teachter) throws GeneralSecurityException, IOException {
		String myTeacher = teachter.split(" ").length > 1 ? teachter.split(" ")[1] : teachter;
		return this.mCalendarUtils.listEvents().stream()
				.filter(element -> element.getTeacher().toLowerCase().contains(myTeacher.toLowerCase()))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("deprecation")
	public List<Course> listSubjectByDay(final String dateString) throws GeneralSecurityException, IOException {

		if (Strings.isNullOrEmpty(dateString)) {
			List<Course> courses = mCalendarUtils.listEvents();
			List<Course> newList = new ArrayList<Course>();
			if (courses.size() > 0) {
				newList.add(courses.get(0));
			}
			return newList;
		}
		return this.mCalendarUtils.listEvents().stream()
				.filter(element -> filterByDate(dateString, element.getStartTime())).collect(Collectors.toList());
	}

	@SuppressWarnings("deprecation")
	private boolean filterByDate(final String dateString, final Date dateToCompare) {
		String[] dateArray = dateString.split("-");
		if (dateArray.length == 3 && !dateArray[1].startsWith(KW)) {
			int year = Integer.parseInt(dateArray[0]);
			int month = Integer.parseInt(dateArray[1]);
			int day = Integer.parseInt(dateArray[2]);
			return dateToCompare.getDate() == day && (dateToCompare.getMonth() + 1 == month)
					&& (dateToCompare.getYear() + 1900 == year);
		} else if (dateArray.length == 2 && dateArray[1].startsWith(KW)) {
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.setTime(dateToCompare);
			return mCalendar.get(Calendar.WEEK_OF_YEAR) == Integer.parseInt(dateArray[1].replaceAll(KW, ""));
		}
		return false;
	}

//	public static BDEvent getInstance() {
//		if (mBdEvent != null) {
//			return mBdEvent;
//		}
//		mBdEvent = new BDEvent();
//		return mBdEvent;
//	}
}
