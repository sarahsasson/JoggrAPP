package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Achievements list
        RecyclerView achievementsRecycler = findViewById(R.id.achievements_recycler);
        achievementsRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<String> achievements = new ArrayList<>();
        achievements.add("🌅 Early Bird - 7 morning workouts");
        achievements.add("🔥 On Fire! - 15-day streak");
        achievements.add("🏆 Top 10 - Local leaderboard");
        achievements.add("🥇 Champion - Completed monthly challenge");

        AchievementsAdapter adapter = new AchievementsAdapter(achievements);
        achievementsRecycler.setAdapter(adapter);

        // Go to Tracking page
        Button goToTrackingButton = findViewById(R.id.go_to_tracking_button);
        goToTrackingButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TrackingActivity.class);
            startActivity(intent);
        });

        // Find Groups page
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

    // Edit Profile button (from XML android:onClick)
    public void onEditProfileClicked(View v) {
        Intent intent = new Intent(
                this,
                com.example.joggrapp.ui.profile.EditProfileActivity.class
        );
        startActivity(intent);
    }
}
