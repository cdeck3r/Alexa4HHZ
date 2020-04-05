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
	private String location;
	private String id;
	private String eTag;
	private boolean cancelled;

	public Course(String description, String organizer, String discipline, Date startTime, String location, String id,
			String eTag) {
		this.description = description;
		this.teacher = organizer;
		this.discipline = discipline;
		this.startTime = startTime;
		this.location = location;
		this.id = id;
		this.eTag = eTag;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean canceled) {
		this.cancelled = canceled;
	}
}
