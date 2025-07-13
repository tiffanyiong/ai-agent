package org.tiff.aiagent.tools.calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "Photography AI Assistant";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // The path to your service account key file in the resources folder.
    private static final String SERVICE_ACCOUNT_KEY_PATH = "/service-account.json";

    @Bean
    public Calendar googleCalendarClient() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load the service account key.
        InputStream in = GoogleCalendarConfig.class.getResourceAsStream(SERVICE_ACCOUNT_KEY_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + SERVICE_ACCOUNT_KEY_PATH);
        }

        // Create credentials from the service account key.
        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        // Build the authorized Calendar client.
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
    