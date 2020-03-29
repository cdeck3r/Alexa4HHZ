
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
	private StringBuilder mStringBuilder;

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListEventByDateIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speechText = "Dein Vorlesungskalendar is nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText).build();
		}
		BDCourse mBdEvent = new BDCourse(requestHelper.getAccountLinkingAccessToken());
		Optional<String> optionalDate = requestHelper.getSlotValue("date");
		mStringBuilder = new StringBuilder();

		try {
			List<Course> myCourse = mBdEvent.listSubjectByDay(optionalDate.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Du hast ");
				mStringBuilder.append(optionalDate.orElse(""));
				mStringBuilder.append(" ");
				mStringBuilder.append("keine Vorlesung");
			} else {
				mStringBuilder.append(optionalDate.orElse("Dem nächst"));
				mStringBuilder.append(" ");
				mStringBuilder.append("hast du ");
				myCourse.forEach(element -> {
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(",");
				});
			}
		} catch (Exception e) {
			mStringBuilder.append(e.getMessage());
		}

		return input.getResponseBuilder().withSpeech(mStringBuilder.toString())
				.withSimpleCard("Vorlesung", mStringBuilder.toString()).build();
	}

}
