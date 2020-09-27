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

import static com.amazon.ask.request.Predicates.requestType;

import java.util.List;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.google.api.client.util.Strings;

import de.hhz.alexa.calendar.calender.BDCourse;
import de.hhz.alexa.calendar.calender.HHZEvent;
import de.hhz.alexa.calendar.utils.Utils;

public class LaunchRequestHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(LaunchRequest.class));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		String speechText = "HHZ Studienkalendar geöffnet. Was möchtest du gern wissen?";
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		if (Strings.isNullOrEmpty(requestHelper.getAccountLinkingAccessToken())) {
			speechText = "Dein Studienkalendar is nicht verknüpft. Verknüpft es bitte über die Skilleinstellung.";
			return input.getResponseBuilder().withSpeech(speechText).withLinkAccountCard().build();
		}
		List<HHZEvent> myCourse = null;
		StringBuilder mStringBuilder = new StringBuilder();
		mStringBuilder.append("<speak>");
		try {
			myCourse = BDCourse.getInstance().getInstanceByUser(requestHelper.getAccountLinkingAccessToken()).listModifiedEvents();
			if (myCourse.size() > 0) {
				mStringBuilder
						.append("HHZ Studienkalendar geöffnet. Achtung. Neue Meldung vom HHZ. Die Veranstaltung ");
				myCourse.forEach(element -> {
					mStringBuilder.append(element.getDescription());
					if (element.isCancelled()) {
						mStringBuilder.append(" ist ausgefallen.");
					} else if (element.isPosponed()) {
						mStringBuilder.append(" wurde verschoben. Neuen Termin:");
						String dateString = Utils.parseDateSimplified(element.getStartTime());
						mStringBuilder.append("<say-as interpret-as='date'>" + dateString.split(",")[0] + "</say-as>");
						mStringBuilder.append(" ");
						mStringBuilder.append(dateString.split(",")[1]);
						mStringBuilder.append(".");
					}
				});
				mStringBuilder.append("</speak>");
				speechText = mStringBuilder.toString();
			}
		} catch (Exception e) {
			mStringBuilder.append("Ein Fehler is beim starten des Skills aufgetretten");
			mStringBuilder.append("</speak>");
			speechText = mStringBuilder.toString();

		}

		return input.getResponseBuilder().withSpeech(speechText).withReprompt("Was möchtest du erfahren? Für alle Fragen, sag einfach Hilfe").build();
	}
}
