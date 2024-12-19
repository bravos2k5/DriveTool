package com.bravos2k5.drivetool.core.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriveAuthenticator {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/drive");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private Credential credential;

    private DriveAuthenticator() throws Exception {
        credential = authorize();
    }

    private static class SingletonHelper {
        private static final DriveAuthenticator INSTANCE;
        static {
            try {
                INSTANCE = new DriveAuthenticator();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static DriveAuthenticator getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public Drive getDrive() {
        try {

            if(credential == null || credential.getAccessToken() == null) {
                this.logOut();
                credential = authorize();
            }

            return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),JSON_FACTORY,credential)
                    .setApplicationName("Drive Tool")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Credential authorize() throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new FileReader(CREDENTIALS_FILE_PATH));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public void logOut() {
        File file = new File("tokens/StoredCredential");
        file.setWritable(true);
        file.delete();
        credential = null;
    }

}
