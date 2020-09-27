package de.hhz.alexa.calendar.calender;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.api.client.util.Strings;

public class BDCourse {
	private static final String KW = "W";
	private static final CharSequence SUFFIX_EXAM = "prüfung";
	private static BDCourse mBDCourse;
	private CalendarUtils mCalendarUtils;
	private static Map<String, BDCourse> instances = new HashMap<String, BDCourse>();

	private BDCourse(final String accesToken) throws Exception {
		this.mCalendarUtils = new CalendarUtils(accesToken);
	}

	private BDCourse() throws Exception {
	}

	public List<HHZEvent> listLecturesByTeacher(final String teachter, final String semester) throws Exception {
		String[] myTeacherArray = teachter.split(" ");
		String myTeacher = myTeacherArray.length > 1 ? myTeacherArray[myTeacherArray.length - 1] : teachter;
		if (Strings.isNullOrEmpty(semester)) {
			return this.mCalendarUtils.listEvents().stream()
					.filter(element -> this.isNextEvent(element) && element.isCourse()
							&& element.getOrganizer().toLowerCase().contains(myTeacher.toLowerCase()))
					.limit(1).collect(Collectors.toList());
		} else {
			return this.mCalendarUtils.listEvents().stream()
					.filter(element -> this.isNextEvent(element) && element.isCourse()
							&& element.getSemester().contains(semester)
							&& element.getOrganizer().toLowerCase().contains(myTeacher.toLowerCase()))
					.limit(1).collect(Collectors.toList());
		}

	}

	public boolean isNextEvent(HHZEvent event) {
		return event.getStartTime().after(new Date());
	}

	/**
	 * List courses
	 */
	@SuppressWarnings("deprecation")
	public List<HHZEvent> listLectureByDate(final String dateString, final String semester) throws Exception {
		List<HHZEvent> courses = mCalendarUtils.listEvents();
		if (Strings.isNullOrEmpty(dateString)) {
			courses = courses.stream().filter(element -> element.isCourse()).limit(1).collect(Collectors.toList());
		} else if (!Strings.isNullOrEmpty(dateString) && Strings.isNullOrEmpty(semester)) {
			courses = courses.stream()
					.filter(element -> element.isCourse() && filterByDate(dateString, element.getStartTime()))
					.collect(Collectors.toList());
		} else if (!Strings.isNullOrEmpty(dateString) && !Strings.isNullOrEmpty(semester)) {
			courses = courses.stream().filter(element -> element.isCourse()
					&& filterByDate(dateString, element.getStartTime()) && element.getSemester().contains(semester))
					.collect(Collectors.toList());
		}

		return courses;
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
		if (subject.toLowerCase().equals("event")) {
			return this.mCalendarUtils.listEvents().stream().filter(element -> !element.isCourse()).limit(1)
					.collect(Collectors.toList());
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
	public List<HHZEvent> listLectureByDateAndSemester(String semester, String dateString) throws Exception {
		if (!Strings.isNullOrEmpty(semester) && !Strings.isNullOrEmpty(dateString)) {
			return this.mCalendarUtils.listEvents().stream()
					.filter(element -> isNextEvent(element) && element.isCourse()
							&& filterByDate(dateString, element.getStartTime())
							&& element.getSemester().contains(semester))
					.collect(Collectors.toList());
		}
		if (!Strings.isNullOrEmpty(semester)) {
			return this.mCalendarUtils.listEvents().stream().filter(
					element -> isNextEvent(element) && element.isCourse() && element.getSemester().contains(semester))
					.limit(1).collect(Collectors.toList());
		}

		if (!Strings.isNullOrEmpty(dateString)) {
			return this.mCalendarUtils.listEvents().stream().filter(element -> isNextEvent(element)
					&& element.isCourse() && filterByDate(dateString, element.getStartTime()))
					.collect(Collectors.toList());
		}

		return this.mCalendarUtils.listEvents().stream().filter(element -> isNextEvent(element) && element.isCourse())
				.limit(1).collect(Collectors.toList());

	}

	/**
	 * List the exams
	 * 
	 * @throws Exception
	 */
	public List<HHZEvent> listExams(final String semester, final String dateString) throws Exception {
		if (!Strings.isNullOrEmpty(semester) && !Strings.isNullOrEmpty(dateString)) {
			return this.mCalendarUtils.listEvents().stream()
					.filter(element -> isNextEvent(element) && element.isCourse() && element.getType() != null
							&& element.getType().toLowerCase().contains(SUFFIX_EXAM)
							&& filterByDate(dateString, element.getStartTime())
							&& element.getSemester().contains(semester))
					.collect(Collectors.toList());
		}
		if (!Strings.isNullOrEmpty(semester)) {
			return this.mCalendarUtils.listEvents().stream()
					.filter(element -> isNextEvent(element) && element.isCourse() && element.getType() != null
							&& element.getType().toLowerCase().contains(SUFFIX_EXAM)
							&& element.getSemester().contains(semester))
					.limit(1).collect(Collectors.toList());
		}

		if (!Strings.isNullOrEmpty(dateString)) {
			return this.mCalendarUtils.listEvents().stream()
					.filter(element -> isNextEvent(element) && element.isCourse() && element.getType() != null
							&& element.getType().toLowerCase().contains(SUFFIX_EXAM)
							&& filterByDate(dateString, element.getStartTime()))
					.collect(Collectors.toList());
		}

		return this.mCalendarUtils
				.listEvents().stream().filter(element -> isNextEvent(element) && element.isCourse()
						&& element.getType() != null && element.getType().toLowerCase().contains(SUFFIX_EXAM))
				.limit(1).collect(Collectors.toList());

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

	public static BDCourse getInstance() throws Exception {
		if (mBDCourse != null) {
			return mBDCourse;
		}
		mBDCourse = new BDCourse();
		return mBDCourse;
	}

	public BDCourse getInstanceByUser(String token) throws Exception {
		if (instances.containsKey(token)) {
			return instances.get(token);
		}
		BDCourse mBDCourse = new BDCourse(token);
		instances.put(token, mBDCourse);
		return mBDCourse;

	}
	public void removeUserInstance(String token) throws Exception {
		if (instances.containsKey(token)) {
			 instances.remove(token);
		}
	}
}
