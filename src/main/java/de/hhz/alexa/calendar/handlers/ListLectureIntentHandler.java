
package de.hhz.alexa.calendar.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;

import de.hhz.alexa.calendar.utils.Utils;

public class ListLectureIntentHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListLectureIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speechText = "Dein Vorlesungskalendar ist nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText).build();
		}
		String token = requestHelper.getAccountLinkingAccessToken();
		Optional<String> optionalDate = requestHelper.getSlotValue("date");
		Optional<String> optionalSemester = requestHelper.getSlotValue("semesterNumber");
		Optional<String> optionalTeacher = requestHelper.getSlotValue("teacher");
		Optional<String> optionalEventType = requestHelper.getSlotValue("eventType");

		String event = optionalEventType.orElse("");
		if (event.toLowerCase().contains("prüfung") || event.toLowerCase().contains("klasur")) {
			return ExamBuilder.build(input, token, optionalSemester, optionalDate);

		}
		if (optionalTeacher.isPresent()) {
			return LectureByTeacherBuilder.build(input, token, optionalTeacher, optionalSemester);
		} else if (optionalDate.isPresent() || optionalSemester.isPresent() || event.toLowerCase().contains("vorlesung")
				|| event.toLowerCase().contains("vorlesen")) {
			return LectureByDayBuilder.build(input, token, optionalSemester, optionalDate);
		}
		return input.getResponseBuilder().withSpeech("Das weiß ich leider nicht.")
				.withReprompt(Utils.REPROMT).build();
	}

}
