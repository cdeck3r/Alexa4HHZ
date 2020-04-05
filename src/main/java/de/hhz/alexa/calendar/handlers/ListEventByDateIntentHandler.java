
package de.hhz.alexa.calendar.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;
import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.Course;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

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
		Optional<String> optionalDate = requestHelper.getSlotValue("date");
		mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			List<Course> myCourse = BDCourse.getInstance(requestHelper.getAccountLinkingAccessToken())
					.listSubjects(optionalDate.orElse(""));
			if (myCourse.size() < 1) {
				mStringBuilder.append("Du hast ");
				// mStringBuilder.append(optionalDate.orElse(""));
				mStringBuilder.append(" ");
				mStringBuilder.append("keine Vorlesung");
			} else {
//				mStringBuilder.append(optionalDate.orElse("Dem nächst"));
//				mStringBuilder.append(" ");
				mStringBuilder.append("Du hast ");
				myCourse.forEach(element -> {
					String dateString = parseDate(element.getStartTime());
					mStringBuilder.append("am ");
					mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
					mStringBuilder.append(" um ");
					mStringBuilder.append(dateString.split(",")[1]);
					mStringBuilder.append(" ");
					mStringBuilder.append(element.getDescription());
					mStringBuilder.append(" ");
					mStringBuilder.append("in ");
					mStringBuilder.append(element.getLocation().replaceAll("[(,)]", ""));
					mStringBuilder.append(",");
				});
			}
		} catch (Exception e) {
			mStringBuilder.append(e.getMessage());
		}
		mStringBuilder.append("</speak>");
		return input.getResponseBuilder().withSpeech(mStringBuilder.toString())
				.withSimpleCard("Vorlesung", mStringBuilder.toString()).build();
	}

	public static String parseDate(final Date date) {
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String dateString = sdf.format(date);
		return dateString.replace(dateString.substring(0, 4), "????");
	}
}
