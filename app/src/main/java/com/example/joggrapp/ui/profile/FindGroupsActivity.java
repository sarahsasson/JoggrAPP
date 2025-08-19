package com.example.joggrapp.ui.profile;

import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joggrapp.R;
import com.example.joggrapp.SuggestedGroupsAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FindGroupsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration groupsRegistration;

    private RecyclerView groupsRecycler;
    private SuggestedGroupsAdapter groupsAdapter;
    private final List<String> groupItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_groups);

        db = FirebaseFirestore.getInstance();

        groupsRecycler = findViewById(R.id.find_groups_recycler);
        groupsRecycler.setLayoutManager(new LinearLayoutManager(this));
        groupsAdapter = new SuggestedGroupsAdapter(groupItems);
        groupsRecycler.setAdapter(groupsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Single orderBy to avoid composite index requirement
        groupsRegistration = db.collection("groupSuggestions")
                .orderBy("memberCount", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("FindGroupsActivity", "Groups query failed", e);
                        Toast.makeText(this, "Failed to load groups", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;

                    groupItems.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String icon = safe(doc.getString("icon"), "👥");
                        String name = safe(doc.getString("name"), "Group");
                        String desc = safe(doc.getString("description"), "");
                        groupItems.add(icon + " " + name + "\n" + desc);
                    }
                    groupsAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (groupsRegistration != null) {
            groupsRegistration.remove();
            groupsRegistration = null;
        }
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }
}
