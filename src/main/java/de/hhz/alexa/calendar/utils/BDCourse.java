package de.hhz.alexa.calendar.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BDCourse {
//	private static BDEvent mBdEvent;
	private CalendarUtils mCalendarUtils;

	public BDCourse(String accessTocken) {
		this.mCalendarUtils = new CalendarUtils(accessTocken);
	}

	public List<Course> listSubjects() throws GeneralSecurityException, IOException {

		return this.mCalendarUtils.listEvents();
	}

	public List<Course> listSubjects(String teachter) throws GeneralSecurityException, IOException {
		String myTeacher = teachter.split(" ").length > 1 ? teachter.split(" ")[1] : teachter;
		return this.mCalendarUtils.listEvents().stream()
				.filter(element -> element.getTeacher().toLowerCase().contains(myTeacher.toLowerCase()))
				.collect(Collectors.toList());
	}

	public List<Course> listSubjectByDay(String dateString) throws GeneralSecurityException, IOException {
		return this.mCalendarUtils.listEvents().stream()
				.filter(element -> filterByDate(dateString, element.getStartTime())).collect(Collectors.toList());
	}

	private boolean filterByDate(String dateString, Date dateToCompare) {
		String[] dateArray = dateString.split("-");
		int year = Integer.parseInt(dateArray[0]);
		int month = Integer.parseInt(dateArray[1]);
		int day = Integer.parseInt(dateArray[2]);
		return dateToCompare.getDate() == day && (dateToCompare.getMonth() + 1 == month)
				&& (dateToCompare.getYear() + 1900 == year);
	}

//	public static BDEvent getInstance() {
//		if (mBdEvent != null) {
//			return mBdEvent;
//		}
//		mBdEvent = new BDEvent();
//		return mBdEvent;
//	}
}
