package com.sourav.hacknovation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTeamsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTeam;
    private TextView tvTotalTeams;
    private TeamAdapter adapter;

    private final List<TeamModel> teamList = new ArrayList<>();
    private final List<String> teamKeys = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_teams, container, false);

        Context context = getContext();
        if (context == null) return view;

        recyclerView = view.findViewById(R.id.recyclerTeams);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);

        adapter = new TeamAdapter(context, new TeamAdapter.OnTeamActionListener() {
            @Override
            public void onEdit(TeamModel team, String teamKey) {
                Intent intent = new Intent(context, AddMembers.class);
                intent.putExtra("TEAM_KEY", teamKey);
                intent.putExtra("IS_EDIT", true);
                startActivity(intent);
            }

            @Override
            public void onDelete(String teamKey) {
                confirmDelete(teamKey);
            }
        });

        recyclerView.setAdapter(adapter);

        tvTotalTeams = view.findViewById(R.id.tvTotalTeams);

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
        });

        fabAddTeam = view.findViewById(R.id.fabAddTeam);
        fabAddTeam.setOnClickListener(v ->
                startActivity(new Intent(context, AddMembers.class))
        );

        loadTeams();
        return view;
    }

    private void loadTeams() {

        FirebaseDatabase.getInstance()
                .getReference("Teams")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!isAdded()) return;

                        teamList.clear();
                        teamKeys.clear();

                        tvTotalTeams.setText("TOTAL TEAMS (" + snapshot.getChildrenCount() + ")");

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TeamModel team = ds.getValue(TeamModel.class);
                            if (team != null) {
                                teamList.add(team);
                                teamKeys.add(ds.getKey());
                            }
                        }

                        adapter.setData(teamList, teamKeys);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvTotalTeams.setText("TOTAL TEAMS (0)");
                    }
                });
    }

    private void confirmDelete(String teamKey) {

        if (teamKey == null) {
            Toast.makeText(getContext(), "Invalid team", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Team")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) ->
                        FirebaseDatabase.getInstance()
                                .getReference("Teams")
                                .child(teamKey)
                                .removeValue()
                                .addOnSuccessListener(v ->
                                        Toast.makeText(getContext(),
                                                "Team deleted", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
