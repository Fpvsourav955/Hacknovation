package com.sourav.hacknovation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {

    private List<TeamModel> list;

    public RankAdapter(List<TeamModel> list) {
        this.list = list;
    }

    public void updateList(List<TeamModel> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamModel team = list.get(position);

        // Use the team's actual rank, not the position in the list
        holder.tvRank.setText(String.valueOf(team.rank));
        holder.tvTeamName.setText(team.teamName);
        holder.tvTheme.setText(team.theme != null ? team.theme.toUpperCase() : "");
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTeamName, tvTheme;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTheme = itemView.findViewById(R.id.tvTheme);
        }
    }
}