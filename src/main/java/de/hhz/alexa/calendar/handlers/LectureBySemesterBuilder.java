
package de.hhz.alexa.calendar.handlers;

import java.util.List;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;

import de.hhz.alexa.calendar.utils.AppConstants;
import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

public class LectureBySemesterBuilder{
	private static StringBuilder mStringBuilder;
	public static Optional<Response> build(HandlerInput input, String token, Optional<String> optionalSemester) {

	
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance()
					.getInstanceByUser(token)
					.listLectureBySemester(optionalSemester.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Es gibt keine Vorlesung für das Semester ");
				mStringBuilder.append(optionalSemester.orElse(""));
			} else {
				mStringBuilder.append("Die nächste Vorlesung");
				if (optionalSemester.isPresent()) {
					mStringBuilder.append(" des " + AppConstants.ORDINAL.get(optionalSemester.get()));
					mStringBuilder.append(" Semesters ");
				}
				mStringBuilder.append(" ist ");
				myCourse.forEach(element -> {
					String dateString = Utils.parseDate(element.getStartTime());
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(" am ");
					mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					mStringBuilder.append(" um ");
					mStringBuilder.append(dateString.split(",")[1]);
					mStringBuilder.append(" ");
					mStringBuilder.append(Utils.getLocation(element.getLocation()));
					mStringBuilder.append(". ");
				});
			}
		} catch (Exception e) {
			mStringBuilder.append(e.getMessage());
		}
		mStringBuilder.append("</speak>");
		return input.getResponseBuilder().withSpeech(mStringBuilder.toString()).withReprompt(Utils.REPROMT).build();
	}

}
