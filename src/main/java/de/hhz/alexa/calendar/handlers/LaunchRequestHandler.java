/*
     Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

     Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
     except in compliance with the License. A copy of the License is located at

         http://aws.amazon.com/apache2.0/

     or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
     the specific language governing permissions and limitations under the License.
*/

package de.hhz.alexa.calendar.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;

import de.hhz.alexa.calendar.utils.BDCourse;
import de.hhz.alexa.calendar.utils.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

import java.util.List;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;

public class LaunchRequestHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(LaunchRequest.class));
	}

	@Override // geänderte events in datenbank speichern und als notification melden.
				// notification erst nach bestätigung löschen.
	public Optional<Response> handle(HandlerInput input) {
		String speechText = "Willkommen zu HHZ Studienkalendar. Du kannst Informationen zu deinen Vorlesungen fragen. Sag z.B. Vorlesung.";
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			speechText = "Dein Vorlesungskalendar is nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
		}
		List<HHZEvent> myCourse = null;
		StringBuilder mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			myCourse = BDCourse.getInstance(requestHelper.getAccountLinkingAccessToken()).listModifiedEvents();
			if (myCourse != null & myCourse.size() > 0) {
				mStringBuilder.append("Deine Vorlesung wurde verschoben.");
				myCourse.forEach(element -> {
					String dateString = Utils.parseDate(element.getStartTime());
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
				mStringBuilder.append("</speak>");

				speechText = mStringBuilder.toString();
			}
		} catch (Exception e) {
			mStringBuilder.append(e.getMessage());
			mStringBuilder.append("</speak>");
			speechText = mStringBuilder.toString();

		}

		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Vorlesung", speechText)
				.withReprompt("Sag z.B. Vorlesung").build();
	}

}
