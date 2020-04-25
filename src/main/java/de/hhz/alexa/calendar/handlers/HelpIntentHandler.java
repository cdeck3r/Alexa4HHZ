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
import com.amazon.ask.model.Response;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = "Du kannst mir folgende Fragen stellen: Was ist n�chste Vorlesung, "
        		+ "Wann ist die n�chste Vorlesung von z.B. Herr Professor Decker, "
        		+ "Wann gibt es die n�chste Vorlesung von Semester 2, "
        		+ "Wann findet die n�chste Pr�fung statt, "
        		+ "Wann findet z.B. Artificial Intelligence statt, "
        		+ "Was ist das n�chste Event.";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("hilfe", speechText)
                .withReprompt(speechText)
                .build();
    }
}
