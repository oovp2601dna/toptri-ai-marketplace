package com.toptri;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class FirebaseInit {

  @Value("${firebase.service-account}")
  private Resource serviceAccount;

  @PostConstruct
  public void init() throws Exception {
    if (!FirebaseApp.getApps().isEmpty()) return;

    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
        .build();

    FirebaseApp.initializeApp(options);
    System.out.println("🔥 Firebase initialized");
  }
}
