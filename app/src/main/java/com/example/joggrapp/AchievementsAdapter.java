package com.example.joggrapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
    private List<String> achievementsList;

    public AchievementsAdapter(List<String> achievementsList) {
        this.achievementsList = achievementsList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView achievementText;

        public ViewHolder(View itemView) {
            super(itemView);
            achievementText = itemView.findViewById(R.id.achievement_text);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String achievement = achievementsList.get(position);
        holder.achievementText.setText(achievement);
    }

    @Override
    public int getItemCount() {
        return achievementsList.size();
    }
}
