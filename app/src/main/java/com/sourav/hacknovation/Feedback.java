package com.sourav.hacknovation;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Feedback extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference feedbackRef;
    LoadingDialog loadingDialog;
    RatingBar ratingOverall;
    EditText edtComment;
    TextView txtOverallText;

    int mentorship = 0, food = 0, organization = 0, technical = 0,application=0;
    String userEmail = "unknown";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_feedback);
        loadingDialog= new LoadingDialog(this);

        auth = FirebaseAuth.getInstance();
        feedbackRef = FirebaseDatabase.getInstance()
                .getReference("feedbacks");

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            userEmail = user.getEmail();
        }
        ImageView aboutback= findViewById(R.id.aboutback);
        aboutback.setOnClickListener(v->{
            finish();

        });
        ratingOverall = findViewById(R.id.ratingOverall);
        edtComment = findViewById(R.id.edtComment);
        txtOverallText = findViewById(R.id.txtOverallText);

        ratingOverall.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            int v = (int) rating;
            String[] txt = {
                    "", "Very Poor (1/5)", "Poor (2/5)",
                    "Good (3/5)", "Very Good (4/5)", "Excellent (5/5)"
            };
            txtOverallText.setText(txt[v]);
            txtOverallText.setTextColor(
                    ContextCompat.getColor(this, R.color.persianblue)
            );
        });

        setupSelector(new int[]{R.id.m1,R.id.m2,R.id.m3,R.id.m4,R.id.m5}, v -> mentorship=v);
        setupSelector(new int[]{R.id.m11,R.id.m21,R.id.m31,R.id.m41,R.id.m51}, v -> food=v);
        setupSelector(new int[]{R.id.m12,R.id.m22,R.id.m32,R.id.m42,R.id.m52}, v -> organization=v);
        setupSelector(new int[]{R.id.m13,R.id.m23,R.id.m33,R.id.m43,R.id.m53}, v -> technical=v);
        setupSelector(new int[]{R.id.m14,R.id.m24,R.id.m34,R.id.m44,R.id.m54}, v -> application=v);

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            loadingDialog.startLoadingDiloag();
                    submitData();

                });
        getWindow().getDecorView().post(() -> {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(true);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets s = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(s.left, s.top, s.right, s.bottom);
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

    private void setupSelector(int[] ids, OnRatingSelected cb) {

        TextView[] views = new TextView[ids.length];

        for (int i = 0; i < ids.length; i++) {
            views[i] = findViewById(ids[i]);
            int value = i + 1;

            views[i].setOnClickListener(v -> {


                for (TextView t : views) {
                    t.setSelected(false);
                    t.setScaleX(1f);
                    t.setScaleY(1f);
                }


                v.setSelected(true);
                cb.onSelect(value);

                v.animate()
                        .scaleX(1.25f)
                        .scaleY(1.25f)
                        .setDuration(90)
                        .withEndAction(() ->
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(90)
                                        .start()
                        )
                        .start();
            });
        }
    }


    private void submitData() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this,"User not logged in",Toast.LENGTH_LONG).show();
            return;
        }

        if (ratingOverall.getRating()==0 || mentorship==0 || food==0
                || organization==0 || technical==0|| application==0) {
            Toast.makeText(this,"Please rate all sections",Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,"Saving feedback…",Toast.LENGTH_SHORT).show();

        String key = feedbackRef.push().getKey();

        Map<String,Object> data = new HashMap<>();
        data.put("event","Hacknovation 2.0");
        data.put("email",userEmail);
        data.put("overall",(int)ratingOverall.getRating());
        data.put("mentorship",mentorship);
        data.put("food",food);
        data.put("organization",organization);
        data.put("technical",technical);
        data.put("application",application);
        data.put("comment",edtComment.getText().toString());
        data.put("timestamp",System.currentTimeMillis());

        feedbackRef.child(key).setValue(data)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this,"Feedback Submitted!",Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }


    interface OnRatingSelected {
        void onSelect(int value);
    }
}
