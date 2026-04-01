package com.sourav.hacknovation;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditTeamActivity extends AppCompatActivity {

    EditText etTeamName, etProjectId, etLeaderName, etRollNumber, etEmail, etPhone;
    EditText member1name, member1roll, member1email;
    EditText member2name, member2roll, member2email;
    EditText member3name, member3roll, member3email;
    Spinner spinnerGenderleader, spinnerGender1, spinnerGender2, spinnerGender3;

    Spinner spinnerTheme;
    LinearLayout member1, member2, member3;
    String member1Key = null;
    String member2Key = null;
    String member3Key = null;

    AppCompatButton btnUpdateTeam;

    DatabaseReference teamRef;
    String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_edit_team);

        teamId = getIntent().getStringExtra("teamId");
        if (teamId == null) {
            Toast.makeText(this, "Team ID missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        teamRef = FirebaseDatabase.getInstance()
                .getReference("Teams")
                .child(teamId);
        ImageView back= findViewById(R.id.back);
        back.setOnClickListener(v->{
            finish();
        });

        initViews();
        setupGenderSpinners();

        setupThemeSpinner();
        fetchTeamData();

        btnUpdateTeam.setOnClickListener(v -> updateTeam());
        getWindow().getDecorView().post(() -> {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(true);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        int nightModeFlags =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        } else {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }
    }
    private void initViews() {

        etTeamName = findViewById(R.id.etTeamName);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        etProjectId = findViewById(R.id.etProjectId);

        spinnerGenderleader = findViewById(R.id.spinnerGenderleader);
        spinnerGender1 = findViewById(R.id.spinnerGender1);
        spinnerGender2 = findViewById(R.id.spinnerGender2);
        spinnerGender3 = findViewById(R.id.spinnerGender3);

        etLeaderName = findViewById(R.id.etLeaderName);
        etRollNumber = findViewById(R.id.etRollNumber);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);

        member1 = findViewById(R.id.member1);
        member2 = findViewById(R.id.member2);
        member3 = findViewById(R.id.member3);

        member1name = findViewById(R.id.member1name);
        member1roll = findViewById(R.id.member1roll);
        member1email = findViewById(R.id.member1email);

        member2name = findViewById(R.id.member2name);
        member2roll = findViewById(R.id.member2roll);
        member2email = findViewById(R.id.member2email);

        member3name = findViewById(R.id.member3name);
        member3roll = findViewById(R.id.member3roll);
        member3email = findViewById(R.id.member3email);

        btnUpdateTeam = findViewById(R.id.btnCreateTeam);
    }

    private void setupThemeSpinner() {

        List<String> themes = new ArrayList<>();
        themes.add("Select Theme");
        themes.add("Agriculture");
        themes.add("Healthcare");
        themes.add("CyberSecurity");
        themes.add("Blockchain");
        themes.add("EduTech");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                themes
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTheme.setAdapter(adapter);
    }

    private void fetchTeamData() {

        teamRef.get().addOnSuccessListener(team -> {

            etTeamName.setText(team.child("teamName").getValue(String.class));
            etProjectId.setText(team.child("projectId").getValue(String.class));
            etLeaderName.setText(team.child("leader").getValue(String.class));
            etRollNumber.setText(team.child("leaderRoll").getValue(String.class));
            etEmail.setText(team.child("leaderEmail").getValue(String.class));
            etPhone.setText(team.child("leaderPhone").getValue(String.class));
            setSpinnerValue(spinnerGenderleader,
                    team.child("leaderGender").getValue(String.class));


            setSpinnerValue(spinnerTheme, team.child("theme").getValue(String.class));

            Long membersCountObj = team.child("membersCount").getValue(Long.class);
            long membersCount = membersCountObj != null ? membersCountObj : 0;

            member1.setVisibility(View.GONE);
            member2.setVisibility(View.GONE);
            member3.setVisibility(View.GONE);
            int index = 0;
            for (DataSnapshot mem : team.child("members").getChildren()) {

                String key = mem.getKey();

                if (index == 0 && membersCount >= 1) {
                    member1Key = key;
                    member1.setVisibility(View.VISIBLE);
                    member1name.setText(mem.child("name").getValue(String.class));
                    member1roll.setText(mem.child("roll").getValue(String.class));
                    member1email.setText(mem.child("email").getValue(String.class));
                    setSpinnerValue(spinnerGender1,
                            mem.child("gender").getValue(String.class));

                }
                else if (index == 1 && membersCount >= 2) {
                    member2Key = key;

                    member2.setVisibility(View.VISIBLE);
                    member2name.setText(mem.child("name").getValue(String.class));
                    member2roll.setText(mem.child("roll").getValue(String.class));
                    member2email.setText(mem.child("email").getValue(String.class));
                    setSpinnerValue(spinnerGender2,
                            mem.child("gender").getValue(String.class));

                }
                else if (index == 2 && membersCount >= 3) {
                    member3Key = key;

                    member3.setVisibility(View.VISIBLE);
                    member3name.setText(mem.child("name").getValue(String.class));
                    member3roll.setText(mem.child("roll").getValue(String.class));
                    member3email.setText(mem.child("email").getValue(String.class));
                    setSpinnerValue(spinnerGender3,
                            mem.child("gender").getValue(String.class));

                }
                index++;
            }

        });
    }
    private void updateTeam() {

        Map<String, Object> updates = new HashMap<>();

        updates.put("teamName", etTeamName.getText().toString().trim());
        updates.put("theme", spinnerTheme.getSelectedItem().toString());
        updates.put("projectId", etProjectId.getText().toString().trim());
        updates.put("leaderGender",
                spinnerGenderleader.getSelectedItem().toString());


        updates.put("leader", etLeaderName.getText().toString().trim());
        updates.put("leaderRoll", etRollNumber.getText().toString().trim());
        updates.put("leaderEmail", etEmail.getText().toString().trim());
        updates.put("leaderPhone", etPhone.getText().toString().trim());

        if (member1.getVisibility() == View.VISIBLE && member1Key != null) {
            updates.put("members/" + member1Key + "/name", member1name.getText().toString());
            updates.put("members/" + member1Key + "/roll", member1roll.getText().toString());
            updates.put("members/" + member1Key + "/email", member1email.getText().toString());
            updates.put("members/" + member1Key + "/gender",
                    spinnerGender1.getSelectedItem().toString());

        }

        if (member2.getVisibility() == View.VISIBLE && member2Key != null) {
            updates.put("members/" + member2Key + "/name", member2name.getText().toString());
            updates.put("members/" + member2Key + "/roll", member2roll.getText().toString());
            updates.put("members/" + member2Key + "/email", member2email.getText().toString());
            updates.put("members/" + member2Key + "/gender",
                    spinnerGender2.getSelectedItem().toString());

        }

        if (member3.getVisibility() == View.VISIBLE && member3Key != null) {
            updates.put("members/" + member3Key + "/name", member3name.getText().toString());
            updates.put("members/" + member3Key + "/roll", member3roll.getText().toString());
            updates.put("members/" + member3Key + "/email", member3email.getText().toString());
            updates.put("members/" + member3Key + "/gender",
                    spinnerGender3.getSelectedItem().toString());

        }


        teamRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Team updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void setupGenderSpinners() {

        List<String> genders = new ArrayList<>();
        genders.add("Select Gender");
        genders.add("Male");
        genders.add("Female");
        genders.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genders
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerGenderleader.setAdapter(adapter);
        spinnerGender1.setAdapter(adapter);
        spinnerGender2.setAdapter(adapter);
        spinnerGender3.setAdapter(adapter);
    }

    private void setSpinnerValue(Spinner spinner, String value) {

        if (value == null) {
            spinner.setSelection(0);
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString().trim();
            if (item.equalsIgnoreCase(value.trim())) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }
}
