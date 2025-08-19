package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity {

    // Firestore
    private FirebaseFirestore db;
    private ListenerRegistration feedRegistration;
    private ListenerRegistration groupsRegistration;

    // UI / adapters
    private RecyclerView activityFeedRecycler;
    private RecyclerView suggestedGroupsRecycler;
    private ActivityFeedAdapter activityAdapter;
    private SuggestedGroupsAdapter groupsAdapter;

    // Backing lists
    private final List<String> activityFeedItems = new ArrayList<>();
    private final List<String> suggestedGroupItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        db = FirebaseFirestore.getInstance();

        // Buttons
        Button yourProfileButton = findViewById(R.id.see_profile_button);
        yourProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(TrackingActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Button fullOverviewButton = findViewById(R.id.full_overview_button);
        fullOverviewButton.setOnClickListener(v ->
                Toast.makeText(this, "Overview coming soon", Toast.LENGTH_SHORT).show()
        );

        Button findGroupsButton = findViewById(R.id.find_groups_button);
        findGroupsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TrackingActivity.this,
                    com.example.joggrapp.ui.profile.FindGroupsActivity.class
            );
            startActivity(intent);
        });

        // Activity Feed RecyclerView
        activityFeedRecycler = findViewById(R.id.activity_feed_recycler);
        activityFeedRecycler.setLayoutManager(new LinearLayoutManager(this));
        activityAdapter = new ActivityFeedAdapter(activityFeedItems);
        activityFeedRecycler.setAdapter(activityAdapter);

        // Suggested Groups RecyclerView
        suggestedGroupsRecycler = findViewById(R.id.suggested_groups_recycler);
        suggestedGroupsRecycler.setLayoutManager(new LinearLayoutManager(this));
        groupsAdapter = new SuggestedGroupsAdapter(suggestedGroupItems);
        suggestedGroupsRecycler.setAdapter(groupsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRealtimeListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachRealtimeListeners();
    }

    private void attachRealtimeListeners() {
        // --- Activity feed: newest first ---
        feedRegistration = db.collection("activityFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("TrackingActivity", "Feed query failed", e);
                            Toast.makeText(TrackingActivity.this,
                                    "Failed to load activity feed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (snapshots == null) return;

                        activityFeedItems.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String icon = safe(doc.getString("icon"), "🔥");
                            String user = safe(doc.getString("userName"), "User");
                            String text = safe(doc.getString("activityText"), "");
                            activityFeedItems.add(icon + " " + user + " " + text);
                        }
                        activityAdapter.notifyDataSetChanged();
                    }
                });

        // --- Group suggestions: order by memberCount only (no composite index needed) ---
        groupsRegistration = db.collection("groupSuggestions")
                .orderBy("memberCount", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("TrackingActivity", "Groups query failed", e);
                        Toast.makeText(TrackingActivity.this,
                                "Failed to load groups", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;

                    suggestedGroupItems.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String icon = safe(doc.getString("icon"), "👥");
                        String name = safe(doc.getString("name"), "Group");
                        String desc = safe(doc.getString("description"), "");
                        suggestedGroupItems.add(icon + " " + name + "\n" + desc);
                    }
                    groupsAdapter.notifyDataSetChanged();
                });
    }

    private void detachRealtimeListeners() {
        if (feedRegistration != null) { feedRegistration.remove(); feedRegistration = null; }
        if (groupsRegistration != null) { groupsRegistration.remove(); groupsRegistration = null; }
    }

    private static String safe(@Nullable String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }
}
