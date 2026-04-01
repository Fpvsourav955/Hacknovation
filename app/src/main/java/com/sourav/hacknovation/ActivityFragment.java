package com.sourav.hacknovation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    private ImageView userImage;
    private TextView userName;
    private TextView tvAttendanceStatus;
    private View notificationDot;
    private NestedScrollView nestedScrollView;

    private RecyclerView rvUpdates;
    private UpdateAdapter adapter;
    private List<UpdateModel> list = new ArrayList<>();
    private DatabaseReference updatesRef;

    private EditText etMessage;
    private ImageView btnSend;
    private LinearLayout layoutSend;

    // ── Food card views ──────────────────────────────────────────────────────────
    private TextView tvFoodTime;
    private TextView tvFoodStatus;
    private ValueEventListener foodListener;
    private DatabaseReference foodRef;

    private DatabaseReference databaseReference;
    private boolean isAdmin = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        userImage           = view.findViewById(R.id.userimage);
        userName            = view.findViewById(R.id.username);
        tvAttendanceStatus  = view.findViewById(R.id.tvAttendanceStatus);
        notificationDot     = view.findViewById(R.id.notificationDot);

        rvUpdates   = view.findViewById(R.id.rvUpdates);
        etMessage   = view.findViewById(R.id.etMessage);
        btnSend     = view.findViewById(R.id.btnSend);
        layoutSend  = view.findViewById(R.id.layoutSend);
        nestedScrollView = view.findViewById(R.id.yourNestedScrollId);

        // Food card
        tvFoodTime   = view.findViewById(R.id.timestring);
        tvFoodStatus = view.findViewById(R.id.statusfood);

        ImageView notification = view.findViewById(R.id.notificationIcon);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvUpdates.setLayoutManager(layoutManager);

        adapter = new UpdateAdapter(list);
        rvUpdates.setAdapter(adapter);

        updatesRef       = FirebaseDatabase.getInstance().getReference("QuickUpdates");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Firebase node for food card:
        // root/
        //   foodCard/
        //     foodTime:   "Lunch 1:00 PM"   ← any string you set in Firebase
        //     foodStatus: "upcoming"         ← "upcoming" | "ongoing" | "completed"
        foodRef = FirebaseDatabase.getInstance().getReference("foodCard");

        notification.setOnClickListener(v -> {
            if (adapter.getItemCount() > 0) {
                rvUpdates.scrollToPosition(adapter.getItemCount() - 1);
            }
            nestedScrollView.post(() ->
                    nestedScrollView.smoothScrollTo(0, nestedScrollView.getBottom())
            );
            notificationDot.setVisibility(View.GONE);
        });

        checkAdminAccess();
        setupSendButton();
        loadUpdates();
        observeAttendanceStatus();
        observeFoodCard();      // ← NEW
        loadGoogleUser();

        MaterialCardView attendance  = view.findViewById(R.id.attendance);
        MaterialCardView food        = view.findViewById(R.id.food);
        MaterialCardView problempdf  = view.findViewById(R.id.problempdf);
        MaterialCardView pdfopen     = view.findViewById(R.id.pdfopen);

        pdfopen.setOnClickListener(v -> openPdfFromRaw(R.raw.agenda, "agenda.pdf"));

        attendance.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), Attendence.class)));

        food.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), Food.class)));

        problempdf.setOnClickListener(v -> {
            String fileId = "1YM1C6_zPydVHGQBX5z9rAvfjDgQo-87p";
            String pdfUrl = "https://drive.google.com/viewerng/viewer?embedded=true&url="
                    + "https://drive.google.com/uc?export=download&id=" + fileId;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl)));
        });

        return view;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FOOD CARD — real-time Firebase listener
    // Firebase structure:
    //   foodCard/
    //     foodTime:   "Lunch 1:00 PM"
    //     foodStatus: "upcoming"   →  blue  text
    //                 "ongoing"    →  red   text
    //                 "completed"  →  green text
    // ─────────────────────────────────────────────────────────────────────────────
    private void observeFoodCard() {
        foodListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                // ── Time string ──────────────────────────────────────────────────
                String time = snapshot.child("foodTime").getValue(String.class);
                if (time != null && !time.isEmpty()) {
                    tvFoodTime.setText(time);
                }

                // ── Status ───────────────────────────────────────────────────────
                // 0 = Upcoming (blue), 1 = Going On (red), 2 = Completed (green)
                Integer statusCode = snapshot.child("foodStatus").getValue(Integer.class);
                if (statusCode != null) {
                    switch (statusCode) {
                        case 1:
                            tvFoodStatus.setText("Going On");
                            tvFoodStatus.setTextColor(
                                    requireContext().getColor(android.R.color.holo_red_dark));
                            break;

                        case 2:
                            tvFoodStatus.setText("Completed");
                            tvFoodStatus.setTextColor(
                                    requireContext().getColor(android.R.color.holo_green_dark));
                            break;

                        case 0:
                        default:
                            tvFoodStatus.setText("Upcoming");
                            // Blue — #5C6BC0 (matches your XML default)
                            tvFoodStatus.setTextColor(0xFF5C6BC0);
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Keep default XML values if Firebase fails
            }
        };

        foodRef.addValueEventListener(foodListener);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ADMIN CHECK
    // ─────────────────────────────────────────────────────────────────────────────
    private void checkAdminAccess() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("Admin")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isAdmin = snapshot.exists();
                        layoutSend.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SEND BUTTON
    // ─────────────────────────────────────────────────────────────────────────────
    private void setupSendButton() {
        btnSend.setOnClickListener(v -> {
            if (!isAdmin) return;
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            String name     = user.getDisplayName() != null ? user.getDisplayName() : "Admin";
            String photoUrl = user.getPhotoUrl()    != null ? user.getPhotoUrl().toString() : "";

            UpdateModel model = new UpdateModel(name, msg, System.currentTimeMillis(), photoUrl);
            updatesRef.push().setValue(model);
            etMessage.setText("");
        });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // QUICK UPDATES
    // ─────────────────────────────────────────────────────────────────────────────
    private void loadUpdates() {
        updatesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    UpdateModel m = s.getValue(UpdateModel.class);
                    if (m != null) list.add(m);
                }
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() > 0) {
                    notificationDot.setVisibility(View.VISIBLE);
                    rvUpdates.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // USER PROFILE
    // ─────────────────────────────────────────────────────────────────────────────
    private void loadGoogleUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (user.getDisplayName() != null) {
            userName.setText("Hi, " + user.getDisplayName() + "!");
        }

        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(userImage);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ATTENDANCE STATUS
    // ─────────────────────────────────────────────────────────────────────────────
    private void observeAttendanceStatus() {
        databaseReference.child("attendanceMarked")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        Boolean isMarked = snapshot.getValue(Boolean.class);
                        if (isMarked != null && isMarked) {
                            tvAttendanceStatus.setText("Marked");
                            tvAttendanceStatus.setTextColor(
                                    requireContext().getColor(android.R.color.holo_green_dark));
                        } else {
                            tvAttendanceStatus.setText("Pending");
                            tvAttendanceStatus.setTextColor(
                                    requireContext().getColor(android.R.color.holo_red_dark));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!isAdded()) return;
                        tvAttendanceStatus.setText("Error");
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // PDF OPEN
    // ─────────────────────────────────────────────────────────────────────────────
    private void openPdfFromRaw(int rawResId, String fileName) {
        try {
            File file = new File(requireContext().getCacheDir(), fileName);

            if (!file.exists()) {
                InputStream inputStream   = getResources().openRawResource(rawResId);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                inputStream.close();
                outputStream.close();
            }

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CLEANUP — remove Firebase listeners to avoid memory leaks
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove food card listener
        if (foodRef != null && foodListener != null) {
            foodRef.removeEventListener(foodListener);
        }
    }
}