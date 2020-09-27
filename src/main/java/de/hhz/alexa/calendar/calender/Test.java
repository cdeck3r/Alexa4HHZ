package de.hhz.alexa.calendar.calender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

public class Test {

    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
	private static final String APPLICATION_NAME = "Alexa4HHZ";

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = Test.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8880).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
	
	public static void main(String args[]) throws Exception {
//		Runner runner =new Runner();
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Credential credentials=getCredentials(HTTP_TRANSPORT);
		String token = credentials.getAccessToken();
		
		BDCourse bdCourse= BDCourse.getInstance().getInstanceByUser(token);
//		DataSourceFactory.getInstance().setUser(bdCourse.getEmail());
		bdCourse.listModifiedEvents().forEach(c->{System.out.println(c.getDescription());System.out.println(c.getId());});

		bdCourse.listEventByName("testvorlesung").forEach(c->{System.out.println(c.getDescription());});

//		HHZEvent event=DataSourceFactory.getInstance().loadEvents(10).get(1);
//		System.out.println(event.getId()+"--"+event.getUser()+"-- "+event.geteTag());
//		event.seteTag("1230");
//		DataSourceFactory.getInstance().updateEvent(event);
//		System.out.println("--------------------------");
//		bdCourse.listModifiedEvents().forEach(c->{System.out.println(c.getDescription());System.out.println(c.getId());});

		
		
		

	}
}
