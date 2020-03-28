
package de.hhz.alexa.calendar.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;
import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.Course;

import java.util.List;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ListEventByDateIntentHandler implements RequestHandler {
	StringBuilder sb = new StringBuilder();
	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListEventByDateIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speech = "Der Kalendar is nicht verknüpft. Verknüpfen Sie es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speech).withSimpleCard("Vorlesung", speech).build();
		}
		BDCourse mBdEvent = new BDCourse(requestHelper.getAccountLinkingAccessToken());
		Optional<String> optionalDate = requestHelper.getSlotValue("date");

		if (!optionalDate.isEmpty()) {
			sb.append(optionalDate.get());
			sb.append(" ");
			sb.append("hast du");
			try {
				List<Course> myCourse = mBdEvent.listSubjects(optionalDate.get());
				myCourse.forEach(element -> {
					sb.append(element.getDescription());
					sb.append(",");
				});

				if (myCourse.size() < 1) {
					sb = new StringBuilder();
					sb.append("Du hast ");
					sb.append(optionalDate.get());
					sb.append(" ");
					sb.append("keine Vorlesung");
				}
			} catch (Exception e) {
				sb.append(e.getMessage());
			}
		}
		return input.getResponseBuilder().withSpeech(sb.toString()).withSimpleCard("Vorlesung", sb.toString()).build();
	}

}
