
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

public class ListEventByNameIntentHandler implements RequestHandler {
	private StringBuilder mStringBuilder;

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ListEventByNameIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			String speechText = "Dein konto is nicht verknüpt. Um dieses skill nutzen zu können, verknüpft es bitte über die Skilleinstellung in Alexa App.";
			return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText).build();
		}
		Optional<String> name = requestHelper.getSlotValue("name");
		if (name.isEmpty()) {
			return input.getResponseBuilder().withSpeech("Ich habe dich nicht verstanden").withReprompt(Utils.REPROMT)
					.build();
		}
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<HHZEvent> myCourse = BDCourse.getInstance().getInstanceByUser(requestHelper.getAccountLinkingAccessToken())
					.listEventByName(name.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Es gibt keine Veranstaltung mit dem Name ");
				mStringBuilder.append(name.get());
			} else {
				mStringBuilder.append("am ");
				myCourse.forEach(element -> {
					String dateString = Utils.parseDate(element.getStartTime());
					mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					mStringBuilder.append(" um ");
					mStringBuilder.append(dateString.split(",")[1]);
					mStringBuilder.append(" ist ");
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(" ");
					mStringBuilder.append(Utils.getLocation(element.getLocation()));
					if (element.isCourse()) {
						mStringBuilder.append(" beim Professor ");
						mStringBuilder.append(element.getOrganizer().substring(element.getOrganizer().indexOf(".") + 1,
								element.getOrganizer().length()));
					}
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
