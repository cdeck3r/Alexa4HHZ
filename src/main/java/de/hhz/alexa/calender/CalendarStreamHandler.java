/*
     Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

     Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
     except in compliance with the License. A copy of the License is located at

         http://aws.amazon.com/apache2.0/

     or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
     the specific language governing permissions and limitations under the License.
*/

package de.hhz.alexa.calender;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;

import de.hhz.alexa.calendar.handlers.CancelandStopIntentHandler;
import de.hhz.alexa.calendar.handlers.FallbackIntentHandler;
import de.hhz.alexa.calendar.handlers.HelpIntentHandler;
import de.hhz.alexa.calendar.handlers.LaunchRequestHandler;
import de.hhz.alexa.calendar.handlers.ListEventByDateIntentHandler;
import de.hhz.alexa.calendar.handlers.SessionEndedRequestHandler;

import com.amazon.ask.SkillStreamHandler;

//mvn assembly:assembly -DdescriptorId=jar-with-dependencies package

public class CalendarStreamHandler extends SkillStreamHandler {

    @SuppressWarnings("unchecked")
	private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        new CancelandStopIntentHandler(),
                        new ListEventByDateIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler(),
                        new FallbackIntentHandler())
                // Add your skill id below
                //.withSkillId("")
                .build();
    }

    public CalendarStreamHandler() {
        super(getSkill());
    }

}
