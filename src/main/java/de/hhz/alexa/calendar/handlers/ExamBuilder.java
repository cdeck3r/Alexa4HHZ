
package de.hhz.alexa.calendar.handlers;

import java.util.List;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;

import de.hhz.alexa.calendar.calender.AppConstants;
import de.hhz.alexa.calendar.calender.BDCourse;
import de.hhz.alexa.calendar.calender.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

public class ExamBuilder {
	private static StringBuilder mStringBuilder;

	public static Optional<Response> build(HandlerInput input, String token, Optional<String> optionalSemester,
			Optional<String> optionalDate) {

		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance().getInstanceByUser(token)
					.listExams(optionalSemester.orElse(""),optionalDate.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Es gibt ");
				if (optionalDate.isPresent()) {
					mStringBuilder.append(Utils.translateDate(optionalDate.get()));
					mStringBuilder.append(" ");
				}
				if (optionalSemester.isPresent() && !optionalSemester.get().equals("?")) {
					mStringBuilder.append("im ");
					mStringBuilder.append(AppConstants.ORDINAL.get(optionalSemester.get()));
					mStringBuilder.append(" Semester ");
				}
				mStringBuilder.append(" keine Prüfung. ");


			} else {
				if (optionalDate.isPresent()) {
					mStringBuilder.append(Utils.translateDate(optionalDate.get()));
					mStringBuilder.append(" ist ");

				} else {
						mStringBuilder.append("Die nächste Prüfung ");
					}
				if (optionalSemester.isPresent()) {
					mStringBuilder.append(" im ");
					mStringBuilder.append(AppConstants.ORDINAL.get(optionalSemester.get()));
					mStringBuilder.append(" Semester ");
				}
				if(optionalDate.isEmpty()) {
				mStringBuilder.append(" ist ");
				}
				
				myCourse.forEach(element -> {
					String dateString = Utils.parseDateSimplified(element.getStartTime());
					mStringBuilder.append(element.getDescription());
					if (optionalDate.isEmpty() || (optionalDate.isPresent() && (!Utils.translateDate(optionalDate.get()).equals("heute")
							&& !Utils.translateDate(optionalDate.get()).equals("morgen")))) {
						mStringBuilder.append(" am ");
						mStringBuilder.append(Utils.parseDateToDayWeek((element.getStartTime())));
					}
					mStringBuilder.append(" ");
					if (optionalDate.isEmpty()) {
						mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					}
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
