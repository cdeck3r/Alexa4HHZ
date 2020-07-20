
package de.hhz.alexa.calendar.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.amazonaws.endpointdiscovery.Constants;
import com.google.api.client.util.Strings;

import de.hhz.alexa.calendar.utils.AppConstants;
import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static com.amazon.ask.request.Predicates.intentName;

public class ListLectureBySemesterIntentHandler implements RequestHandler {
	private StringBuilder mStringBuilder;

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListLectureBySemesterIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speechText = "Dein Vorlesungskalendar is nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText).build();
		}
		Optional<String> optionalSemester = requestHelper.getSlotValue("semesterNumber");
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance()
					.getInstanceByUser(requestHelper.getAccountLinkingAccessToken())
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
