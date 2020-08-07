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

import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;
import com.amazon.ask.request.RequestHelper;

import de.hhz.alexa.calendar.utils.BDCourse;

public class SessionEndedRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(SessionEndedRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		try {
			BDCourse.getInstance().removeUserInstance(requestHelper.getAccountLinkingAccessToken());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
        return input.getResponseBuilder().withSpeech("").build();
    }

}
