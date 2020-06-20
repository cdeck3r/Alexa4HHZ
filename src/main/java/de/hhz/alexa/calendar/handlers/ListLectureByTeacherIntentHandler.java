
package de.hhz.alexa.calendar.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;

import de.hhz.alexa.calendar.utils.AppConstants;
import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;
import java.util.List;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ListLectureByTeacherIntentHandler implements RequestHandler {
	private StringBuilder mStringBuilder;

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListLectureByTeacherIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speechText = "Dein Vorlesungskalendar is nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText).build();
		}
		Optional<String> optionalTeacher = requestHelper.getSlotValue("teacher");
		Optional<String> optionalSemester = requestHelper.getSlotValue("semesterNumber");

		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance(requestHelper.getAccountLinkingAccessToken())
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
					String dateString = Utils.parseDate(element.getStartTime());
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(" am ");
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
