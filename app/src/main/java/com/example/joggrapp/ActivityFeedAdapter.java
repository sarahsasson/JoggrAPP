package com.example.joggrapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ActivityFeedAdapter extends RecyclerView.Adapter<ActivityFeedAdapter.ViewHolder> {
    private final List<String> activityFeed;

    public ActivityFeedAdapter(List<String> activityFeed) {
        this.activityFeed = activityFeed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_feed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.activityText.setText(activityFeed.get(position));
    }

    @Override
    public int getItemCount() {
        return activityFeed.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityText = itemView.findViewById(R.id.activity_text);
        }
    }
}
