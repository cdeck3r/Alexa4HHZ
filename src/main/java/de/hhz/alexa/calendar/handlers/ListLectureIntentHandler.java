
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
		Optional<Response> response = null;
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

		String event = optionalEventType.get();
		if (event.toLowerCase().contains("prüfung") || event.toLowerCase().contains("klasur")) {
			return ExamBuilder.build(input, token, optionalSemester,optionalDate);

		}
		if (optionalDate.isPresent() || optionalSemester.isPresent()||event.toLowerCase().contains("vorlesung")
				|| event.toLowerCase().contains("vorlesen")) {
			response = LectureByDayBuilder.build(input, token, optionalSemester, optionalDate);
		} else if (optionalTeacher.isPresent()) {
			response = LectureByTeacherBuilder.build(input, token, optionalTeacher, optionalSemester);
		} else {
			response = input.getResponseBuilder().withSpeech("Ich habe leider zu dieser Frage keine Antwort")
					.withReprompt(Utils.REPROMT).build();

		}
		return response;
	}

}
