package com.sourav.hacknovation;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AddMembers extends AppCompatActivity {
    EditText etTeamName, etProjectId, etLeaderName, etRollNumber,
            etEmail, etPhone,
            member1name, member2name, member3name,
            member1roll, member2roll, member3roll,
            member1email, member2email, member3email;
    Spinner spinnerGenderLeader, spinnerGender1, spinnerGender2, spinnerGender3;

    LoadingDialog loadingDialog;
    Spinner spinnerTheme, spinnerMemberCount;
    private boolean isEdit = false;
    private String teamKey = null;
    private boolean isRestoring = false;

    Button btnCreateTeam;


    DatabaseReference teamsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_add_members);
        teamsRef = FirebaseDatabase.getInstance().getReference("Teams");
        getWindow().getDecorView().post(() -> {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(true);
        });
        isEdit = getIntent().getBooleanExtra("IS_EDIT", false);
        teamKey = getIntent().getStringExtra("TEAM_KEY");

        loadingDialog= new LoadingDialog(this);
        member1name = findViewById(R.id.member1name);
        member2name = findViewById(R.id.member2name);
        member3name = findViewById(R.id.member3name);

        spinnerGenderLeader = findViewById(R.id.spinnerGenderleader);
        spinnerGender1 = findViewById(R.id.spinnerGender1);
        spinnerGender2 = findViewById(R.id.spinnerGender2);
        spinnerGender3 = findViewById(R.id.spinnerGender3);


        member1roll = findViewById(R.id.member1roll);
        member2roll = findViewById(R.id.member2roll);
        member3roll = findViewById(R.id.member3roll);

        member1email = findViewById(R.id.member1email);
        member2email = findViewById(R.id.member2email);
        member3email = findViewById(R.id.member3email);

        spinnerTheme = findViewById(R.id.spinnerTheme);
         spinnerMemberCount = findViewById(R.id.spinnerMemberCount);
        etTeamName = findViewById(R.id.etTeamName);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        etProjectId = findViewById(R.id.etProjectId);
        etLeaderName = findViewById(R.id.etLeaderName);
        etRollNumber = findViewById(R.id.etRollNumber);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        LinearLayout member1 = findViewById(R.id.member1);
        LinearLayout member2 = findViewById(R.id.member2);
        LinearLayout member3 = findViewById(R.id.member3);
        ImageView back = findViewById(R.id.back);

        member1.setVisibility(View.GONE);
        member2.setVisibility(View.GONE);
        member3.setVisibility(View.GONE);

        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        if (isEdit && teamKey != null) {
            fetchTeamData(teamKey);
            btnCreateTeam.setText("Update Team");
        }

        btnCreateTeam.setOnClickListener(v ->{
                loadingDialog.startLoadingDiloag();
        btnCreateTeam.setEnabled(false);
                saveTeam();
        });


        ArrayList<String> themes = new ArrayList<>();
        themes.add("Select Theme");
        themes.add("Agriculture");
        themes.add("Healthcare");
        themes.add("CyberSecurity");
        themes.add("Blockchain");
        themes.add("EduTech");
        ArrayList<String> members = new ArrayList<>();
        members.add("1 Member");
        members.add("2 Members");
        members.add("3 Members");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                members
        );

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMemberCount.setAdapter(adapter1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                themes
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);

        View.OnClickListener smoothBack = v ->
                getOnBackPressedDispatcher().onBackPressed();
        back.setOnClickListener(smoothBack);
        spinnerMemberCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                member1.setVisibility(View.GONE);
                member2.setVisibility(View.GONE);
                member3.setVisibility(View.GONE);

                String selected = parent.getItemAtPosition(position).toString();

                if (selected.equals("1 Member")) {
                    member1.setVisibility(View.VISIBLE);
                }
                else if (selected.equals("2 Members")) {
                    member1.setVisibility(View.VISIBLE);
                    member2.setVisibility(View.VISIBLE);
                }
                else if (selected.equals("3 Members")) {
                    member1.setVisibility(View.VISIBLE);
                    member2.setVisibility(View.VISIBLE);
                    member3.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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
    private void saveTeam() {

        String leader = etLeaderName.getText().toString().trim();
        String projectId = etProjectId.getText().toString().trim();
        String theme = spinnerTheme.getSelectedItem().toString();
        String teamName = etTeamName.getText().toString().trim();
        String leaderEmail = etEmail.getText().toString().trim();
        String leaderPhone = etPhone.getText().toString().trim();
        String leaderRoll = etRollNumber.getText().toString().trim();
        String leaderGender = spinnerGenderLeader.getSelectedItem().toString();
        String gender1 = spinnerGender1.getSelectedItem().toString();
        String gender2 = spinnerGender2.getSelectedItem().toString();
        String gender3 = spinnerGender3.getSelectedItem().toString();


        String memberText = spinnerMemberCount.getSelectedItem().toString();
        int memberCount;

        if (memberText.contains("1")) memberCount = 1;
        else if (memberText.contains("2")) memberCount = 2;
        else if (memberText.contains("3")) memberCount = 3;
        else {
            Toast.makeText(this, "Select member count", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(leader) || TextUtils.isEmpty(projectId)) {
            Toast.makeText(this, "Required fields missing", Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
            btnCreateTeam.setEnabled(true);
            return;
        }
        if (TextUtils.isEmpty(teamName)) {
            Toast.makeText(this, "Team name is required", Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
            btnCreateTeam.setEnabled(true);
            return;
        }
        if (TextUtils.isEmpty(leaderEmail)) {
            Toast.makeText(this, "Leader email is required", Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
            btnCreateTeam.setEnabled(true);
            return;
        }
        if (TextUtils.isEmpty(leaderRoll)) {
            Toast.makeText(this, "Leader roll number is required", Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
            btnCreateTeam.setEnabled(true);
            return;
        }

        if (TextUtils.isEmpty(leaderPhone)) {
            Toast.makeText(this, "Leader phone is required", Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
            btnCreateTeam.setEnabled(true);
            return;
        }

        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                DatabaseReference ref;

                if (isEdit && teamKey != null) {
                    ref = teamsRef.child(teamKey);
                } else {
                    String newKey = teamsRef.push().getKey();
                    if (newKey == null) {
                        loadingDialog.dismissDialog();
                        btnCreateTeam.setEnabled(true);
                        Toast.makeText(AddMembers.this,
                                "Failed to create team key", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ref = teamsRef.child(newKey);
                }

                Map<String, Object> teamData = new HashMap<>();
                teamData.put("teamName", teamName);
                teamData.put("leader", leader);
                teamData.put("leaderRoll", leaderRoll);
                teamData.put("leaderEmail", leaderEmail);
                teamData.put("leaderPhone", leaderPhone);
                teamData.put("membersCount", memberCount);
                teamData.put("leaderGender", leaderGender);

                teamData.put("projectId", projectId);
                teamData.put("theme", theme);
                teamData.put("verified", false);

                Map<String, Object> members = new HashMap<>();

                if (memberCount >= 1 && !TextUtils.isEmpty(member1name.getText()))
                    members.put("member1",
                            createMember(member1name, member1roll, member1email, spinnerGender1));


                if (memberCount >= 2 && !TextUtils.isEmpty(member2name.getText()))
                    members.put("member2",
                            createMember(member2name, member2roll, member2email, spinnerGender2));

                if (memberCount >= 3 && !TextUtils.isEmpty(member3name.getText()))
                    members.put("member3",
                            createMember(member3name, member3roll, member3email, spinnerGender3));

                teamData.put("members", members);

                ref.updateChildren(teamData)

                        .addOnSuccessListener(unused -> {
                            loadingDialog.dismissDialog();
                            showSuccessDialog();
                        })
                        .addOnFailureListener(e -> {
                            loadingDialog.dismissDialog();
                            btnCreateTeam.setEnabled(true);
                            Toast.makeText(AddMembers.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    loadingDialog.dismissDialog();
                    btnCreateTeam.setEnabled(true);
                Toast.makeText(AddMembers.this,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Map<String, String> createMember(EditText name, EditText roll,
                                             EditText email, Spinner genderSpinner) {

        Map<String, String> member = new HashMap<>();
        member.put("name", name.getText().toString().trim());
        member.put("roll", roll.getText().toString().trim());
        member.put("email", email.getText().toString().trim());
        member.put("gender", genderSpinner.getSelectedItem().toString());

        return member;
    }

    private void showSuccessDialog() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.team_success);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            finish();
        }, 2000);
    }
    private void fetchTeamData(String teamKey) {
        isRestoring = true;

        teamsRef.child(teamKey).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        etTeamName.setText(snapshot.child("teamName").getValue(String.class));
                        etLeaderName.setText(snapshot.child("leader").getValue(String.class));
                        etRollNumber.setText(snapshot.child("leaderRoll").getValue(String.class));

                        etEmail.setText(snapshot.child("leaderEmail").getValue(String.class));
                        etPhone.setText(snapshot.child("leaderPhone").getValue(String.class));
                        etProjectId.setText(snapshot.child("projectId").getValue(String.class));

                        String theme = snapshot.child("theme").getValue(String.class);
                        setSpinnerValue(spinnerTheme, theme);
                        String leaderGender = snapshot.child("leaderGender").getValue(String.class);
                        setSpinnerValue(spinnerGenderLeader, leaderGender);

                        Integer membersCountObj = snapshot.child("membersCount")
                                .getValue(Integer.class);

                        int membersCount = membersCountObj != null ? membersCountObj : 0;


                        setSpinnerValue(spinnerMemberCount,
                                membersCount + " Member" + (membersCount > 1 ? "s" : ""));
                        spinnerMemberCount.post(() ->
                                spinnerMemberCount.setSelection(spinnerMemberCount.getSelectedItemPosition()));


                        DataSnapshot membersSnap = snapshot.child("members");

                        if (membersSnap.exists()) {

                            if (membersSnap.hasChild("member1"))
                                fillMember(membersSnap.child("member1"),
                                        member1name, member1roll, member1email, spinnerGender1);

                            if (membersSnap.hasChild("member2"))
                                fillMember(membersSnap.child("member2"),
                                        member2name, member2roll, member2email, spinnerGender2);

                            if (membersSnap.hasChild("member3"))
                                fillMember(membersSnap.child("member3"),
                                        member3name, member3roll, member3email, spinnerGender3);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddMembers.this,
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void fillMember(DataSnapshot snap,
                            EditText name, EditText roll,
                            EditText email, Spinner genderSpinner) {

        name.setText(snap.child("name").getValue(String.class));
        roll.setText(snap.child("roll").getValue(String.class));
        email.setText(snap.child("email").getValue(String.class));

        String gender = snap.child("gender").getValue(String.class);
        setSpinnerValue(genderSpinner, gender);
    }


    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }


}