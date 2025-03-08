package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        Button yourProfileButton = findViewById(R.id.see_profile_button);
        yourProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(TrackingActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        //  Activity Feed RecyclerView
        RecyclerView activityFeedRecycler = findViewById(R.id.activity_feed_recycler);
        activityFeedRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Sample dynamic activity feed data
        List<String> activityFeed = new ArrayList<>();
        activityFeed.add("🏃‍♂️ Alex just completed a 5km run!");
        activityFeed.add("🚴 Lisa joined a cycling group!");
        activityFeed.add("🔥 John has a 10-day streak!");
        activityFeed.add("🏆 Sarah reached 10,000 steps!");

        // Adapter for RecyclerView
        ActivityFeedAdapter activityAdapter = new ActivityFeedAdapter(activityFeed);
        activityFeedRecycler.setAdapter(activityAdapter);

        Button fullOverviewButton = findViewById(R.id.full_overview_button);
        fullOverviewButton.setOnClickListener(v -> {

        });

        Button findGroupsButton = findViewById(R.id.find_groups_button);
        findGroupsButton.setOnClickListener(v -> {

        });

        // Suggested Groups RecyclerView
        RecyclerView suggestedGroupsRecycler = findViewById(R.id.suggested_groups_recycler);
        suggestedGroupsRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Sample dynamic data for suggested groups
        List<String> suggestedGroups = new ArrayList<>();
        suggestedGroups.add("🏃 City Runners\nDaily running group for all skill levels!");
        suggestedGroups.add("🚴 Cycle Squad\nWeekend cycling group for long rides!");

        // adapter for Suggested Groups RecyclerView
        SuggestedGroupsAdapter groupsAdapter = new SuggestedGroupsAdapter(suggestedGroups);
        suggestedGroupsRecycler.setAdapter(groupsAdapter);
    }
}
