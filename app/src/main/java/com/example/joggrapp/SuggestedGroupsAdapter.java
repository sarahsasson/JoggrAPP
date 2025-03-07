package com.example.joggrapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SuggestedGroupsAdapter extends RecyclerView.Adapter<SuggestedGroupsAdapter.ViewHolder> {
    private final List<String> groups;

    public SuggestedGroupsAdapter(List<String> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggested_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.groupName.setText(groups.get(position));
        holder.joinButton.setOnClickListener(v -> {
            // TODO: Handle join group action
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        Button joinButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            joinButton = itemView.findViewById(R.id.join_group_button);
        }
    }
}
