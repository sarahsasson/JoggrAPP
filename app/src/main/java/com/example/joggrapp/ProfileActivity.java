package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Achievements
    private RecyclerView achievementsRecycler;
    private AchievementsAdapter achievementsAdapter;
    private final List<String> achievementsItems = new ArrayList<>();
    private ListenerRegistration achievementsRegistration;
    private ListenerRegistration userDocRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // --- RecyclerView setup (achievements) ---
        achievementsRecycler = findViewById(R.id.achievements_recycler);
        achievementsRecycler.setLayoutManager(new LinearLayoutManager(this));
        achievementsAdapter = new AchievementsAdapter(achievementsItems);
        achievementsRecycler.setAdapter(achievementsAdapter);

        // --- Buttons / navigation ---
        Button goToTrackingButton = findViewById(R.id.go_to_tracking_button);
        if (goToTrackingButton != null) {
            goToTrackingButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, TrackingActivity.class);
                startActivity(intent);
            });
        }

        Button findGroupsButton = findViewById(R.id.btnFindGroups);
        if (findGroupsButton != null) {
            findGroupsButton.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ProfileActivity.this,
                        com.example.joggrapp.ui.profile.FindGroupsActivity.class
                );
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // --- 1) Listen to top-level user doc: users/{uid} ---
        userDocRegistration = db.collection("users")
                .document(user.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("ProfileActivity", "User doc listener failed", e);
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) return;

                    bindUserHeader(snapshot);
                });

        // --- 2) Listen to achievements subcollection: users/{uid}/achievements ---
        achievementsRegistration = db.collection("users")
                .document(user.getUid())
                .collection("achievements")
                .orderBy("earnedAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("ProfileActivity", "Achievements query failed", e);
                        Toast.makeText(this, "Failed to load achievements", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;

                    achievementsItems.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String icon = safe(doc.getString("icon"), "🏅");
                        String title = safe(doc.getString("title"), "Achievement");
                        String desc = safe(doc.getString("description"), "");
                        achievementsItems.add(icon + " " + title + (desc.isEmpty() ? "" : " - " + desc));
                    }
                    achievementsAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (achievementsRegistration != null) {
            achievementsRegistration.remove();
            achievementsRegistration = null;
        }
        if (userDocRegistration != null) {
            userDocRegistration.remove();
            userDocRegistration = null;
        }
    }

    // Edit Profile button (from XML android:onClick)
    public void onEditProfileClicked(View v) {
        Intent intent = new Intent(
                this,
                com.example.joggrapp.ui.profile.EditProfileActivity.class
        );
        startActivity(intent);
    }

    // ----------------- helpers -----------------

    private void bindUserHeader(DocumentSnapshot snap) {
        // Reads with defaults so UI never crashes if a field is missing
        String name = safe(snap.getString("displayName"), "Runner");
        long age = getLong(snap, "age", 0L);
        String tagline = safe(snap.getString("tagline"), "");
        long calories = getLong(snap, "caloriesTotal", 0L);
        long workouts = getLong(snap, "workoutsCount", 0L);
        double distanceKm = getDouble(snap, "distanceKm", 0.0);
        String avatarUrl = safe(snap.getString("avatarUrl"), "");

        // Try a few common id names so you don't have to rename your XML
        setTextIfPresent("tv_name", name);
        setTextIfPresent("profile_name", name);
        setTextIfPresent("name_text", name);

        setTextIfPresent("tv_age", (age > 0 ? age + "y/o" : ""));
        setTextIfPresent("profile_age", (age > 0 ? age + "y/o" : ""));

        setTextIfPresent("tv_tagline", tagline);
        setTextIfPresent("profile_tagline", tagline);

        setTextIfPresent("tv_calories", String.valueOf(calories));
        setTextIfPresent("calories_value", String.valueOf(calories));

        setTextIfPresent("tv_workouts", String.valueOf(workouts));
        setTextIfPresent("workouts_value", String.valueOf(workouts));

        setTextIfPresent("tv_distance", formatDistance(distanceKm));
        setTextIfPresent("distance_value", formatDistance(distanceKm));

        // Avatar (optional): if you already load an image elsewhere, keep doing that.
        // If you use an ImageView with one of these ids, you can plug in Glide/Picasso later.
        setAvatarIfPresent(new String[]{"iv_avatar", "profile_avatar"}, avatarUrl);
    }

    private String formatDistance(double km) {
        // keep "194 km" style like your UI
        return ((km == Math.floor(km)) ? String.valueOf((long) km) : String.format(java.util.Locale.US, "%.1f", km)) + " km";
    }

    private void setTextIfPresent(String idName, String value) {
        int resId = getResources().getIdentifier(idName, "id", getPackageName());
        if (resId != 0) {
            TextView tv = findViewById(resId);
            if (tv != null) tv.setText(value);
        }
    }

    private void setAvatarIfPresent(String[] idNames, String url) {
        if (url.isEmpty()) return;
        for (String idName : idNames) {
            int resId = getResources().getIdentifier(idName, "id", getPackageName());
            if (resId != 0) {
                ImageView iv = findViewById(resId);
                if (iv != null) {
                    // TODO: If you add Glide later:
                    // Glide.with(this).load(url).into(iv);
                }
                return;
            }
        }
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }

    private static long getLong(DocumentSnapshot snap, String field, long fallback) {
        Long v = snap.getLong(field);
        return (v == null) ? fallback : v;
    }

    private static double getDouble(DocumentSnapshot snap, String field, double fallback) {
        Double v = snap.getDouble(field);
        return (v == null) ? fallback : v;
    }
}
