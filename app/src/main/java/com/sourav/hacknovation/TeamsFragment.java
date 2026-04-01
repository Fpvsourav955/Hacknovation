package com.sourav.hacknovation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeamsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EvaluatorTeamAdapter adapter;
    private List<EvaluatorTeamModel> teamList = new ArrayList<>();
    TextView tvTotalTeams;
    public TeamsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerView = view.findViewById(R.id.recyclerTeamsEv);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        EditText etSearch = view.findViewById(R.id.etSearch);
        adapter = new EvaluatorTeamAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        tvTotalTeams = view.findViewById(R.id.tvTotalTeams);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        loadTeamsFromFirebase();

        return view;
    }
    private void loadTeamsFromFirebase() {

        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference().child("Teams");

        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                teamList.clear();
                long totalTeams = snapshot.getChildrenCount();
                tvTotalTeams.setText("ASSIGNED TEAMS (" + totalTeams + ")");
                for (DataSnapshot ds : snapshot.getChildren()) {

                    String teamKey = ds.getKey();   // 🔥 GET FIREBASE KEY

                    EvaluatorTeamModel team = ds.getValue(EvaluatorTeamModel.class);

                    if (team != null) {
                        team.setTeamKey(teamKey);   // 🔥 STORE IT IN MODEL
                        teamList.add(team);
                    }
                }
                adapter.setData(teamList);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalTeams.setText("ASSIGNED TEAMS (0)");
            }
        });
    }


}

