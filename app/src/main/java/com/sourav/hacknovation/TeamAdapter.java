package com.sourav.hacknovation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private final Context context;
    private final OnTeamActionListener listener;

    private final List<TeamModel> teamList = new ArrayList<>();
    private final List<TeamModel> fullTeamList = new ArrayList<>();
    private final List<String> teamKeys = new ArrayList<>();
    private final List<String> fullTeamKeys = new ArrayList<>();

    public TeamAdapter(Context context, OnTeamActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_team_card, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {

        TeamModel team = teamList.get(position);
        String teamKey = teamKeys.get(position);

        holder.txtProjectId.setText("#" + team.projectId);
        holder.txtTeamName.setText(team.teamName);
        holder.txtTheme.setText(team.theme);
        holder.txtLeader.setText("Leader: " + team.leader);
        holder.txtMembers.setText(team.membersCount + " Members");

        holder.txtVerified.setVisibility(team.verified ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(team, teamKey)
        );

        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(teamKey)
        );
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    // 🔥 FIXED setData
    public void setData(List<TeamModel> teams, List<String> keys) {

        teamList.clear();
        teamKeys.clear();
        fullTeamList.clear();
        fullTeamKeys.clear();

        teamList.addAll(teams);
        teamKeys.addAll(keys);

        fullTeamList.addAll(teams);
        fullTeamKeys.addAll(keys);

        notifyDataSetChanged();
    }

    public void filter(String query) {

        teamList.clear();
        teamKeys.clear();

        if (query == null || query.trim().isEmpty()) {
            teamList.addAll(fullTeamList);
            teamKeys.addAll(fullTeamKeys);
        } else {
            query = query.toLowerCase();

            for (int i = 0; i < fullTeamList.size(); i++) {
                TeamModel team = fullTeamList.get(i);

                if ((team.teamName != null && team.teamName.toLowerCase().contains(query)) ||
                        (team.projectId != null && team.projectId.toLowerCase().contains(query))) {

                    teamList.add(team);
                    teamKeys.add(fullTeamKeys.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {

        TextView txtVerified, txtProjectId, txtMembers,
                txtTeamName, txtTheme, txtLeader;
        TextView btnEdit;
        ImageView btnDelete;

        TeamViewHolder(@NonNull View itemView) {
            super(itemView);

            txtVerified = itemView.findViewById(R.id.txtVerified);
            txtProjectId = itemView.findViewById(R.id.txtProjectId);
            txtMembers = itemView.findViewById(R.id.txtMembers);
            txtTeamName = itemView.findViewById(R.id.txtTeamName);
            txtTheme = itemView.findViewById(R.id.txtTheme);
            txtLeader = itemView.findViewById(R.id.txtLeader);

            // ⚠️ TYPES MUST MATCH XML
            btnEdit = itemView.findViewById(R.id.btnEdit1);   // Button/TextView
            btnDelete = itemView.findViewById(R.id.btnDelete1); // ImageView
        }
    }

    public interface OnTeamActionListener {
        void onEdit(TeamModel team, String teamKey);
        void onDelete(String teamKey);
    }
}
