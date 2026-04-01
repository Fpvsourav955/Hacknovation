package com.sourav.hacknovation;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {

    ImageView ivVerify;

    View cardLeader, cardMember2, cardMember3, cardMember4;

    ImageView TeamLeadimg, member2img, member3img, member4img;
    LoadingDialog loadingDialog;
    private String currentTeamId;
    TextView leaderGenderBadge, member2GenderBadge,
            member3GenderBadge, member4GenderBadge;

    TextView leadername, leaderroll,
            member2name, member2roll,
            member3name, member3roll,
            member4name, member4roll,
            tvTeamName, tvthemeandpsid, tvMembers, tvStatus;

    AppCompatButton editteambutton;
    TextView teamtablenumber;

    DatabaseReference teamsRef, usersRef;
    String currentUid, currentUserEmail;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        cardLeader = view.findViewById(R.id.cardLeader);
        cardMember2 = view.findViewById(R.id.cardMember2);
        cardMember3 = view.findViewById(R.id.cardMember3);
        cardMember4 = view.findViewById(R.id.cardMember4);
        editteambutton = view.findViewById(R.id.editteambutton);
       teamtablenumber = view.findViewById(R.id.teamtablenumber);

        loadingDialog= new LoadingDialog(requireActivity());

        leaderGenderBadge = view.findViewById(R.id.leaderGenderBadge);
        member2GenderBadge = view.findViewById(R.id.member2GenderBadge);
        member3GenderBadge = view.findViewById(R.id.member3GenderBadge);
        member4GenderBadge = view.findViewById(R.id.member4GenderBadge);

        cardLeader.setVisibility(View.GONE);
        cardMember2.setVisibility(View.GONE);
        cardMember3.setVisibility(View.GONE);
        cardMember4.setVisibility(View.GONE);
        editteambutton.setVisibility(View.GONE);
        ivVerify = view.findViewById(R.id.ivVerifyTeam);
        ivVerify.setOnClickListener(v -> showVerifyDialog());



        TeamLeadimg = view.findViewById(R.id.TeamLeadimg);
        member2img = view.findViewById(R.id.member2img);
        member3img = view.findViewById(R.id.member3img);
        member4img = view.findViewById(R.id.member4img);


        leadername = view.findViewById(R.id.leadername);
        leaderroll = view.findViewById(R.id.leaderroll);

        member2name = view.findViewById(R.id.member2name);
        member2roll = view.findViewById(R.id.member2roll);

        member3name = view.findViewById(R.id.member3name);
        member3roll = view.findViewById(R.id.member3roll);

        member4name = view.findViewById(R.id.member4name);
        member4roll = view.findViewById(R.id.member4roll);

        tvTeamName = view.findViewById(R.id.tvTeamName);
        tvthemeandpsid = view.findViewById(R.id.tvthemeandpsid);
        tvMembers = view.findViewById(R.id.tvMembers);
        tvStatus = view.findViewById(R.id.tvStatus);
        auth = FirebaseAuth.getInstance();


        FrameLayout logoutButton = view.findViewById(R.id.logoutbutton);

        logoutButton.setOnClickListener(v -> showLogoutDialog());

        currentUid = FirebaseAuth.getInstance().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        teamsRef = FirebaseDatabase.getInstance().getReference("Teams");
        loadingDialog.startLoadingDiloag();
        fetchCurrentUserEmail();
        editteambutton.setOnClickListener(v -> {
            if (currentTeamId == null) return;

            Intent intent = new Intent(requireContext(), EditTeamActivity.class);
            intent.putExtra("teamId", currentTeamId);
            startActivity(intent);
        });


        return view;
    }

    private void fetchCurrentUserEmail() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            loadingDialog.dismissDialog();
            return;
        }

        currentUserEmail = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail();

        if (currentUserEmail == null) {
            loadingDialog.dismissDialog();
            return;
        }

        currentUserEmail = currentUserEmail.trim().toLowerCase();
        findUserTeam();
    }

    private void findUserTeam() {
        teamsRef.get().addOnSuccessListener(snapshot -> {

            for (DataSnapshot teamSnap : snapshot.getChildren()) {

                String leaderEmail = teamSnap.child("leaderEmail").getValue(String.class);
                if (leaderEmail != null &&
                        leaderEmail.trim().toLowerCase().equals(currentUserEmail)) {

                    loadTeam(teamSnap);
                    editteambutton.setVisibility(View.VISIBLE);
                    ivVerify.setVisibility(View.VISIBLE);
                    return;
                }

                for (DataSnapshot mem : teamSnap.child("members").getChildren()) {
                    String email = mem.child("email").getValue(String.class);

                    if (email != null &&
                            email.trim().toLowerCase().equals(currentUserEmail)) {

                        loadTeam(teamSnap);
                        editteambutton.setVisibility(View.GONE);
                        ivVerify.setVisibility(View.GONE);
                        return;
                    }
                }
            }
            loadingDialog.dismissDialog();
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadTeam(DataSnapshot team) {

        if (!isAdded()) return;
        loadingDialog.dismissDialog();

        currentTeamId = team.getKey();

        cardLeader.setVisibility(View.VISIBLE);
        cardMember2.setVisibility(View.GONE);
        cardMember3.setVisibility(View.GONE);
        cardMember4.setVisibility(View.GONE);
        teamtablenumber.setVisibility(View.GONE);
        teamtablenumber.setText("");


        Boolean verified = team.child("verified").getValue(Boolean.class);

        tvStatus.setText(Boolean.TRUE.equals(verified) ? "VERIFIED" : "PENDING");
        tvStatus.setTextColor(
                ContextCompat.getColor(requireContext(),
                        Boolean.TRUE.equals(verified)
                                ? android.R.color.holo_green_dark
                                : android.R.color.holo_red_dark)
        );

        tvTeamName.setText(team.child("teamName").getValue(String.class));
        if (team.hasChild("teamTableNumber")) {

            String tableNumber = team.child("teamTableNumber")
                    .getValue(String.class);

            if (tableNumber != null && !tableNumber.trim().isEmpty()) {

                teamtablenumber.setText(tableNumber.trim());
                teamtablenumber.setVisibility(View.VISIBLE);

            } else {
                teamtablenumber.setVisibility(View.GONE);
            }

        } else {
            teamtablenumber.setVisibility(View.GONE);
        }



        tvthemeandpsid.setText(
                team.child("theme").getValue(String.class) + " · " +
                        team.child("projectId").getValue(String.class)
        );

        Long countObj = team.child("membersCount").getValue(Long.class);
        long count = countObj != null ? countObj : 0;
        tvMembers.setText(count + " MEMBERS");


        leadername.setText(team.child("leader").getValue(String.class));
        leaderroll.setText(team.child("leaderRoll").getValue(String.class));

        String leaderGender = team.child("leaderGender").getValue(String.class);
        setGenderBadge(leaderGenderBadge, leaderGender);

        String leaderEmail = team.child("leaderEmail").getValue(String.class);
        loadProfileImageByEmail(leaderEmail, TeamLeadimg);


        DataSnapshot membersSnap = team.child("members");

        if (membersSnap.hasChild("member1")) {
            cardMember2.setVisibility(View.VISIBLE);
            setMember(membersSnap.child("member1"),
                    member2name, member2roll,
                    member2img, member2GenderBadge);
        }

        if (membersSnap.hasChild("member2")) {
            cardMember3.setVisibility(View.VISIBLE);
            setMember(membersSnap.child("member2"),
                    member3name, member3roll,
                    member3img, member3GenderBadge);
        }

        if (membersSnap.hasChild("member3")) {
            cardMember4.setVisibility(View.VISIBLE);
            setMember(membersSnap.child("member3"),
                    member4name, member4roll,
                    member4img, member4GenderBadge);
        }
    }

    private void setMember(DataSnapshot mem,
                           TextView name,
                           TextView roll,
                           ImageView img,
                           TextView genderBadge) {

        name.setText(mem.child("name").getValue(String.class));
        roll.setText(mem.child("roll").getValue(String.class));

        String gender = mem.child("gender").getValue(String.class);
        setGenderBadge(genderBadge, gender);

        String email = mem.child("email").getValue(String.class);
        loadProfileImageByEmail(email, img);
    }

    private void setGenderBadge(TextView badge, String gender) {

        if (gender == null || gender.trim().isEmpty()
                || gender.equalsIgnoreCase("Select Gender")) {

            badge.setVisibility(View.GONE);
            return;
        }

        badge.setVisibility(View.VISIBLE);

        switch (gender.toLowerCase()) {

            case "male":
                badge.setText("M");
                badge.setBackgroundResource(R.drawable.bg_gender_male);
                break;

            case "female":
                badge.setText("F");
                badge.setBackgroundResource(R.drawable.bg_gender_female);
                break;

            default:
                badge.setText("O");
                badge.setBackgroundResource(R.drawable.bg_gender_other);
                break;
        }
    }


    private void loadProfileImageByEmail(String email, ImageView imageView) {

        if (email == null || email.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.hackimg3bg);
            return;
        }

        email = email.trim().toLowerCase();

        usersRef.orderByChild("email")
                .equalTo(email)
                .limitToFirst(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded() || getView() == null) return;

                    if (!snapshot.exists()) {
                        imageView.setImageResource(R.drawable.hackimg3bg);
                        return;
                    }

                    DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                    String provider = userSnap.child("provider").getValue(String.class);
                    String imageUrl = userSnap.child("profileImage").getValue(String.class);

                    if ("google".equals(provider) && imageUrl != null) {
                        Glide.with(imageView)
                                .load(imageUrl)
                                .placeholder(R.drawable.hackimg3bg)
                                .fallback(R.drawable.hackimg3bg)
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.hackimg3bg);
                    }
                });
    }
    private void showLogoutDialog() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showVerifyDialog() {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_verify_code, null);

        TextView etCode = view.findViewById(R.id.etCode);

        new AlertDialog.Builder(requireContext())
                .setTitle("Verify Team")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Proceed", (dialog, which) -> {

                    String code = etCode.getText().toString().trim();
                    verifyCode(code);
                })
                .show();
    }
    private void verifyCode(String code) {

        if (!"2005".equals(code)) {
            Toast.makeText(requireContext(),
                    "Invalid verification code", Toast.LENGTH_SHORT).show();
            return;
        }

        teamsRef.child(currentTeamId)
                .child("verified")
                .setValue(true)
                .addOnSuccessListener(unused -> {

                    tvStatus.setText("VERIFIED");
                    tvStatus.setSelected(true);
                    tvStatus.setTextColor(
                            ContextCompat.getColor(requireContext(),
                                    android.R.color.holo_green_dark)
                    );

                    ivVerify.setVisibility(View.GONE);

                    Toast.makeText(requireContext(),
                            "Team verified successfully", Toast.LENGTH_SHORT).show();
                });
    }


    private void logoutUser() {

        auth.signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingDialog != null) {
            loadingDialog.dismissDialog();
        }
    }
}
