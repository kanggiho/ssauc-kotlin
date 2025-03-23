package com.example.ssauc.user.login.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public boolean verifyFirebaseToken(String firebaseToken, String phone) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String verifiedPhone = (String) token.getClaims().get("phone_number");
            return phone.equals(verifiedPhone);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return false;
        }
    }
}
