package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
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

        // ✅ Set up RecyclerView for achievements
        RecyclerView achievementsRecycler = findViewById(R.id.achievements_recycler);
        achievementsRecycler.setLayoutManager(new LinearLayoutManager(this));

        // ✅ List of achievements
        List<String> achievements = new ArrayList<>();
        achievements.add("🌅 Early Bird - 7 morning workouts");
        achievements.add("🔥 On Fire! - 15-day streak");
        achievements.add("🏆 Top 10 - Local leaderboard");
        achievements.add("🥇 Champion - Completed monthly challenge");

        // ✅ Set adapter for achievements
        AchievementsAdapter adapter = new AchievementsAdapter(achievements);
        achievementsRecycler.setAdapter(adapter);

        // ✅ Set up button to open TrackingActivity
        Button goToTrackingButton = findViewById(R.id.go_to_tracking_button);
        goToTrackingButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TrackingActivity.class);
            startActivity(intent);
        });
    }
}
