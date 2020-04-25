package de.hhz.alexa.calendar.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.auth.Credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.Strings;

public class BDCourse {
	private static final String KW = "W";
	private static final CharSequence SUFFIX_EXAM = "prüfung";
	private static BDCourse mBDCourse;
	private CalendarUtils mCalendarUtils;

	private BDCourse(final Credential accessTocken) {
		this.mCalendarUtils = new CalendarUtils(accessTocken);
	}

	private BDCourse(final String accessTocken) {
		this.mCalendarUtils = new CalendarUtils(accessTocken);
	}

	public List<HHZEvent> listLecturesByTeacher(final String teachter) throws Exception {
		String[] myTeacherArray = teachter.split(" ");
		String myTeacher = myTeacherArray.length > 1 ? myTeacherArray[myTeacherArray.length - 1] : teachter;

		return this.mCalendarUtils.listEvents().stream().filter(
				element -> element.isCourse() && element.getTeacher().toLowerCase().contains(myTeacher.toLowerCase()))
				.limit(1).collect(Collectors.toList());
	}

	/**
	 * List courses
	 */
	@SuppressWarnings("deprecation")
	public List<HHZEvent> listLectureByDate(final String dateString) throws Exception {
		List<HHZEvent> courses = mCalendarUtils.listEvents();
		if (Strings.isNullOrEmpty(dateString)) {
			courses = courses.stream().filter(element -> element.isCourse()).limit(1).collect(Collectors.toList());
		} else {
			courses = courses.stream()
					.filter(element -> element.isCourse() && filterByDate(dateString, element.getStartTime()))
					.collect(Collectors.toList());
		}

		return courses;
	}

	/**
	 * List events that are not course
	 * 
	 * @param dateString
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public List<HHZEvent> listEventByDate(final String dateString) throws Exception {
		List<HHZEvent> courses = mCalendarUtils.listEvents();
		if (Strings.isNullOrEmpty(dateString)) {
			return courses.stream().filter(element -> !element.isCourse()).limit(1).collect(Collectors.toList());
		}
		return courses.stream()
				.filter(element -> !element.isCourse() && filterByDate(dateString, element.getStartTime())).limit(1)
				.collect(Collectors.toList());
	}

	public List<HHZEvent> listModifiedEvents() throws Exception {
		return mCalendarUtils.listModifiedEvents();

	}

	/**
	 * Return the next date for a given event name
	 * 
	 * @throws Exception
	 */
	public List<HHZEvent> listEventByName(String subject) throws Exception {

		if (Strings.isNullOrEmpty(subject)) {
			return null;
		}
		return this.mCalendarUtils.listEvents().stream()
				.filter(element -> element.getDescription().toLowerCase().contains(subject.toLowerCase())).limit(1)
				.collect(Collectors.toList());
	}

	/**
	 * Return the next course of a given semester.
	 * 
	 * @throws Exception
	 */
	public List<HHZEvent> listLectureBySemester(String semester) throws Exception {
		return this.mCalendarUtils.listEvents().stream()
				.filter(element -> element.isCourse() && element.getSemester().contains(semester)).limit(1)
				.collect(Collectors.toList());
	}

	/**
	 * List the exams
	 * 
	 * @throws Exception
	 */
	public List<HHZEvent> listExams() throws Exception {
		return this.mCalendarUtils.listEvents().stream().filter(element -> element.isCourse()
				&& element.getType() != null && element.getType().toLowerCase().contains(SUFFIX_EXAM)).limit(1)
				.collect(Collectors.toList());
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

	public static BDCourse getInstance(Credential tocken) {
		if (mBDCourse != null) {
			return mBDCourse;
		}
		mBDCourse = new BDCourse(tocken);
		return mBDCourse;
	}

	public static BDCourse getInstance(String tocken) {
		if (mBDCourse != null) {
			return mBDCourse;
		}
		mBDCourse = new BDCourse(tocken);
		return mBDCourse;
	}
}
