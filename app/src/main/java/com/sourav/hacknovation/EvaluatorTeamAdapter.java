package com.sourav.hacknovation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class EvaluatorTeamAdapter extends RecyclerView.Adapter<EvaluatorTeamAdapter.EvaluatorTeamViewHolder> {

    private Context context;
    private List<EvaluatorTeamModel> teamList;
    private List<EvaluatorTeamModel> fullTeamList;

    public EvaluatorTeamAdapter(Context context) {
        this.context = context;
        this.teamList = new ArrayList<>();
        this.fullTeamList = new ArrayList<>();
    }


    @NonNull
    @Override
    public EvaluatorTeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_team_card_evaluator, parent, false);
        return new EvaluatorTeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EvaluatorTeamViewHolder holder, int position) {

        EvaluatorTeamModel team = teamList.get(position);

        holder.txtProjectId.setText("#" + team.projectId);
        holder.txtTeamName.setText(team.teamName);
        holder.txtTheme.setText("Theme: " + team.theme);
        holder.txtLeader.setText("Leader: " + team.leader);

        holder.btnStartEval1.setOnClickListener(v -> {

            Intent intent = new Intent(context, EvaluationActivity.class);

            intent.putExtra("teamKey", team.getTeamKey());   // 🔥 VERY IMPORTANT
            intent.putExtra("projectId", team.projectId);
            intent.putExtra("teamName", team.teamName);

            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }
    public void setData(List<EvaluatorTeamModel> list) {
        fullTeamList.clear();
        fullTeamList.addAll(list);

        teamList.clear();
        teamList.addAll(list);

        notifyDataSetChanged();
    }

    public void filter(String query) {
        teamList.clear();

        if (query.isEmpty()) {
            teamList.addAll(fullTeamList);
        } else {
            query = query.toLowerCase();
            for (EvaluatorTeamModel team : fullTeamList) {
                if (team.teamName.toLowerCase().contains(query)
                        || team.projectId.toLowerCase().contains(query)) {
                    teamList.add(team);
                }
            }
        }
        notifyDataSetChanged();
    }


    static class EvaluatorTeamViewHolder extends RecyclerView.ViewHolder {

        TextView txtVerified, txtProjectId, txtMembers,
                txtTeamName, txtTheme, txtLeader;
        AppCompatButton btnStartEval1;

        public EvaluatorTeamViewHolder(@NonNull View itemView) {
            super(itemView);


            txtProjectId = itemView.findViewById(R.id.txtProjectId);
            btnStartEval1= itemView.findViewById(R.id.btnStartEval1);
            txtTeamName = itemView.findViewById(R.id.txtTeamName);
            txtTheme = itemView.findViewById(R.id.txtTheme);
            txtLeader = itemView.findViewById(R.id.txtLeader);
        }
    }
}
