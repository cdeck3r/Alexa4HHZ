
package de.hhz.alexa.calendar.handlers;

import java.util.List;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;

import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

public class LectureByDayBuilder {
	private static StringBuilder mStringBuilder;

	

	
	public static Optional<Response> build(HandlerInput input, String token, Optional<String> optionalDate, Optional<String>optionalSemester) {
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance()
					.getInstanceByUser(token)
					.listLectureByDate(optionalDate.orElse(""), optionalSemester.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Es gibt keine Vorlesung ");
				if (optionalDate.isPresent()) {
					mStringBuilder.append(Utils.translateDate(optionalDate.get()));
					mStringBuilder.append(" ");
				}
				if (optionalSemester.isPresent()) {
					mStringBuilder.append(" für das semester ");
					mStringBuilder.append(optionalSemester.get());
				}
			} else {
				myCourse.forEach(element -> {
					String dateString = Utils.parseDate(element.getStartTime());
					mStringBuilder.append(" am ");
					mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					mStringBuilder.append(" um ");
					mStringBuilder.append(dateString.split(",")[1]);
					mStringBuilder.append(" ");
					mStringBuilder.append(Utils.getLocation(element.getLocation()));
					mStringBuilder.append(" ist ");
					mStringBuilder.append(element.getDescription());
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
