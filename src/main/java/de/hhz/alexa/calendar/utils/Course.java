package de.hhz.alexa.calendar.utils;

import java.util.Date;
public class Course {
	private String description;
	private String teacher;
	private String discipline;
	private String semester;
	private Date startTime;
	private Date endTime;
	private Date lastModified;

	public Course(String description, String organizer, String discipline, Date startTime) {
		this.description = description;
		this.teacher = organizer;
		this.discipline = discipline;
		this.startTime=startTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTeacher() {
		return teacher;
	}

	public void setOrganizer(String teacher) {
		this.teacher = teacher;
	}

	public String getDiscipline() {
		return discipline;
	}

	public void setDiscipline(String discipline) {
		this.discipline = discipline;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
}
