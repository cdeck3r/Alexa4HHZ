package de.hhz.alexa.calendar.calender;

import java.util.Date;

import com.google.api.client.util.Strings;

public class HHZEvent {
	private String description;
	private String teacher;
	private String semester;
	private Date startTime;
	private Date endTime;
	private Date lastModified;
	private String location;
	private String id;
	private String eTag;
	private boolean cancelled;
	private boolean isCourse;
	private boolean isPosponed;
	private String type;
	private String user;

	public HHZEvent(String description, String organizer, String semester, Date startTime, String location, String id,
			String eTag) {
		this.description = description;
		this.teacher = organizer;
		this.semester = semester;
		this.startTime = startTime;
		this.location = location;
		this.id = id;
		this.eTag = eTag;
	}

	public HHZEvent() {

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOrganizer() {
		return teacher;
	}

	public void setOrganizer(String teacher) {
		this.teacher = teacher;
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

	public boolean isCourse() {
		return isCourse;
	}

	public void setCourse(boolean isCourse) {
		this.isCourse = isCourse;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isPosponed() {
		return isPosponed;
	}

	public void setPosponed(boolean isPosponed) {
		this.isPosponed = isPosponed;
	}
	
    @Override
	public boolean equals(Object o) {
		if (o instanceof HHZEvent) {
			HHZEvent toCompare = (HHZEvent) o;
			return this.id.equals(toCompare.id);
		}
		return false;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
