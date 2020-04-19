
package de.hhz.alexa.calendar.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;
import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;
import java.util.List;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ListEventIntentHandler implements RequestHandler {
	private StringBuilder mStringBuilder;

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListEventIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speechText = "Dein Vorlesungskalendar is nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText).build();
		}
		Optional<String> optionalDate = requestHelper.getSlotValue("date");
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance(requestHelper.getAccountLinkingAccessToken())
					.listEventByDate(optionalDate.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Es gibt kein Event ");
				mStringBuilder.append(optionalDate.orElse(""));
			} else {
				myCourse.forEach(element -> {
					mStringBuilder.append("am ");
					String dateString = Utils.parseDate(element.getStartTime());
					mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					mStringBuilder.append(" um ");
					mStringBuilder.append(dateString.split(",")[1]);
					mStringBuilder.append(" gibt es ");
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(Utils.getLocation(element.getLocation()));
					mStringBuilder.append(".");
				});
			}
		} catch (Exception e) {
			mStringBuilder.append(e.getMessage());
		}
		mStringBuilder.append("</speak>");
		return input.getResponseBuilder().withSpeech(mStringBuilder.toString())
				.withSimpleCard("Vorlesung", mStringBuilder.toString()).build();
	}

}
