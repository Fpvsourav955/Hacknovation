package com.sourav.hacknovation;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Attendence extends AppCompatActivity {

    RecyclerView rvAttendance;
    AttendanceAdapter adapter;
    List<AttendanceModel> list = new ArrayList<>();

    DatabaseReference teamRef;
    String currentDay = "day1";
    LoadingDialog loadingDialog;
    HashMap<String, String> emailToProfileMap = new HashMap<>();
    TextView txtTotalCount, txtPresentCount, txtAbsentCount;
    CardView cardSummary;

    RadioGroup dayGroup;
    AppCompatButton btnSave;

    boolean isAdmin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_attendence);
        loadingDialog = new LoadingDialog(this);

        rvAttendance = findViewById(R.id.rvAttendance);
        dayGroup = findViewById(R.id.dayGroup);
        btnSave = findViewById(R.id.btnSave);
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtPresentCount = findViewById(R.id.txtPresentCount);
        txtAbsentCount = findViewById(R.id.txtAbsentCount);
        cardSummary = findViewById(R.id.cardSummary);


        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(list, isAdmin);
        rvAttendance.setAdapter(adapter);
        EditText etSearch = findViewById(R.id.etSearch);
        teamRef = FirebaseDatabase.getInstance().getReference("Teams");
        loadingDialog.startLoadingDiloag();
        adapter.setOnAttendanceChangeListener(() -> updateSummaryCounts());

        loadUsersProfileMap(() -> loadAttendanceOrTeams(currentDay));


        dayGroup.setOnCheckedChangeListener((group, checkedId) -> {
            loadingDialog.startLoadingDiloag();
            if (checkedId == R.id.rbDay1) {
                currentDay = "day1";
            } else if (checkedId == R.id.rbDay2) {
                currentDay = "day2";
            }
            loadUsersProfileMap(() -> loadAttendanceOrTeams(currentDay));

        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateSummaryCounts();

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnSave.setOnClickListener(v ->
        {
            loadingDialog.startLoadingDiloag();
            saveAttendance(currentDay);
        });
        checkAdminAccess();


        setupSystemUI();
    }
    private void loadAttendanceOrTeams(String day) {

        DatabaseReference attendanceRef = FirebaseDatabase.getInstance()
                .getReference("Attendance")
                .child(day);

        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                list.clear();
                if (snap.exists()) {
                    for (DataSnapshot s : snap.getChildren()) {
                        AttendanceModel m = s.getValue(AttendanceModel.class);
                        if (m != null) {
                            if (m.email != null) {
                                m.profileImage =
                                        emailToProfileMap.get(m.email.toLowerCase());
                            }
                            list.add(m);
                        }
                    }
                    adapter.updateList(list);
                    updateSummaryCounts();

                    loadingDialog.dismissDialog();
                } else {
                    fetchFromTeams();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismissDialog();
            }
        });
    }

    private void fetchFromTeams() {

        teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                list.clear();

                for (DataSnapshot team : snap.getChildren()) {

                    String leaderName = team.child("leader").getValue(String.class);
                    String leaderRoll = team.child("leaderRoll").getValue(String.class);
                    String leaderEmail = team.child("leaderEmail").getValue(String.class);

                    if (leaderName != null && leaderRoll != null) {

                        AttendanceModel leader =
                                new AttendanceModel(leaderName, leaderRoll, leaderEmail);

                        if (leaderEmail != null) {
                            leader.profileImage =
                                    emailToProfileMap.get(leaderEmail.toLowerCase());
                        }

                        list.add(leader);
                    }

                    for (DataSnapshot mem : team.child("members").getChildren()) {

                        String name = mem.child("name").getValue(String.class);
                        String roll = mem.child("roll").getValue(String.class);
                        String email = mem.child("email").getValue(String.class);

                        if (name != null && roll != null) {

                            AttendanceModel member =
                                    new AttendanceModel(name, roll, email);

                            if (email != null) {
                                member.profileImage =
                                        emailToProfileMap.get(email.toLowerCase());
                            }

                            list.add(member);
                        }
                    }
                }

                adapter.updateList(list);
                updateSummaryCounts();

                loadingDialog.dismissDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismissDialog();
            }
        });
    }
    private void updateSummaryCounts() {

        List<AttendanceModel> currentList = adapter.getCurrentList();

        int total = currentList.size();
        int present = 0;

        for (AttendanceModel m : currentList) {
            if (m.present) {
                present++;
            }
        }

        int absent = total - present;

        txtTotalCount.setText(String.valueOf(total));
        txtPresentCount.setText(String.valueOf(present));
        txtAbsentCount.setText(String.valueOf(absent));
    }


    private void saveAttendance(String day) {
        if (!isAdmin) {
            Toast.makeText(this, "Only admin can save attendance", Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
            return;
        }

        DatabaseReference attendanceRef = FirebaseDatabase.getInstance()
                .getReference("Attendance")
                .child(day);

        for (AttendanceModel m : list) {

            if (m.roll == null || m.roll.isEmpty()) continue;

            HashMap<String, Object> map = new HashMap<>();
            map.put("name", m.name);
            map.put("roll", m.roll);
            map.put("present", m.present);

            attendanceRef.child(m.roll).setValue(map);
        }

        Toast.makeText(this,
                "Attendance saved for " + day.toUpperCase(),
                Toast.LENGTH_SHORT).show();
        loadingDialog.dismissDialog();
    }
    private void checkAdminAccess() {

        String uid = FirebaseAuth.getInstance().getUid();

        assert uid != null;
        DatabaseReference adminRef = FirebaseDatabase.getInstance()
                .getReference("Admin")
                .child(uid);

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                isAdmin = snapshot.exists();


                btnSave.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                adapter.setAdmin(isAdmin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void setupSystemUI() {

        getWindow().getDecorView().post(() -> {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(true);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
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
    private void loadUsersProfileMap(Runnable onComplete) {

        DatabaseReference usersRef =
                FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                emailToProfileMap.clear();

                for (DataSnapshot user : snapshot.getChildren()) {

                    String email = user.child("email").getValue(String.class);
                    String profile = user.child("profileImage").getValue(String.class);

                    if (email != null && profile != null) {
                        emailToProfileMap.put(email.toLowerCase(), profile);
                    }
                }

                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

}
