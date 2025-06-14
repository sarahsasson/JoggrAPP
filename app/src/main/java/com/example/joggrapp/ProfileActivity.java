package com.example.joggrapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /* ----------- 1)  Firebase Analytics -------------- */
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Profile Screen");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ProfileActivity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        /* ----------- 2)  Firestore test write ------------- */
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Jack Bauer");
        user.put("age", 29);
        user.put("job", "Fitness Enthusiast");

        db.collection("users")
                .add(user)
                .addOnSuccessListener(docRef ->
                        Log.d("Firestore", "Document added with ID: " + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Error adding document", e));

        /* ----------- 3)  Crashlytics test crash ---------- */
        // Uncomment the lines below whenever you want to generate
        // a crash to verify Crashlytics is still working.
        //
        // throw new RuntimeException("Test Crash: This is only a test!");
    }
}
