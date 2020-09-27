
package de.hhz.alexa.calendar.handlers;

import java.util.List;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;

import de.hhz.alexa.calendar.calender.AppConstants;
import de.hhz.alexa.calendar.calender.BDCourse;
import de.hhz.alexa.calendar.calender.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

public class LectureByTeacherBuilder {
	private static StringBuilder mStringBuilder;

	public static Optional<Response> build(HandlerInput input, String token, Optional<String> optionalTeacher,
			Optional<String> optionalSemester) {
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance().getInstanceByUser(token)
					.listLecturesByTeacher(optionalTeacher.orElse(""), optionalSemester.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append(optionalTeacher.get());
				mStringBuilder.append(" hat keine Vorlesung ");
				if (optionalSemester.isPresent()) {
					mStringBuilder.append(" im ");
					mStringBuilder.append(AppConstants.ORDINAL.get(optionalSemester.get()));
					mStringBuilder.append(" Semester.");
				}
			} else {
				mStringBuilder.append("Die nächste Vorlesung von ");
				mStringBuilder.append(optionalTeacher.get());
				if (optionalSemester.isPresent()) {
					mStringBuilder.append(" im ");
					mStringBuilder.append(AppConstants.ORDINAL.get(optionalSemester.get()));
					mStringBuilder.append(" Semester ");
				}
				mStringBuilder.append(" ist ");

				myCourse.forEach(element -> {
					String dateString = Utils.parseDateSimplified(element.getStartTime());
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(" am ");
					mStringBuilder.append(Utils.parseDateToDayWeek((element.getStartTime())));
					mStringBuilder.append(" ");
					mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					mStringBuilder.append(" ");
					mStringBuilder.append(dateString.split(",")[1]);
					mStringBuilder.append(" ");
					mStringBuilder.append(Utils.getLocation(element.getLocation()));
					mStringBuilder.append(".");
				});
			}
		} catch (Exception e) {
			mStringBuilder.append(e.getMessage());
		}
		mStringBuilder.append("</speak>");
		return input.getResponseBuilder().withSpeech(mStringBuilder.toString()).withReprompt(Utils.REPROMT).build();
	}

}
