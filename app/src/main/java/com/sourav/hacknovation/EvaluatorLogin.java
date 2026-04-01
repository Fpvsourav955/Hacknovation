package com.sourav.hacknovation;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EvaluatorLogin extends AppCompatActivity {
LoadingDialog loadingDialog;
    private static final String EVALUATOR_PASSWORD = "HACKNOVATION2026";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_evaluator_login);
        getWindow().getDecorView().post(() -> {
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(true);
        });
        loadingDialog = new LoadingDialog(this);


        AppCompatEditText etEmail = findViewById(R.id.etEmail);
        AppCompatEditText etSecurityCode = findViewById(R.id.etSecurityCode);
        AppCompatButton btnLogin = findViewById(R.id.login);
        ImageView back = findViewById(R.id.back);
        AppCompatButton backtostart = findViewById(R.id.backtostart);

        View.OnClickListener smoothBack = v ->
                getOnBackPressedDispatcher().onBackPressed();

        back.setOnClickListener(smoothBack);
        backtostart.setOnClickListener(smoothBack);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String inputCode = etSecurityCode.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter email");
                return;
            }

            if (TextUtils.isEmpty(inputCode)) {
                etSecurityCode.setError("Enter security code");
                return;
            }
            loadingDialog.startLoadingDiloag();

            // 🔥 First validate evaluator code
            validateEvaluatorCode(inputCode, email);
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
    private void validateEvaluatorCode(String inputCode, String email) {

        FirebaseDatabase.getInstance()
                .getReference("code")
                .child("evaluator")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        loadingDialog.dismissDialog();
                        Toast.makeText(this,
                                "Evaluator code not configured",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String dbCode = snapshot.getValue(String.class);

                    if (dbCode != null &&
                            dbCode.trim().equalsIgnoreCase(inputCode.trim())) {

                        loginOrCreateEvaluator(email);

                    } else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(this,
                                "Invalid evaluator code",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void loginOrCreateEvaluator(String email) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, EVALUATOR_PASSWORD)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        makeUserEvaluator(user);
                    } else {
                        loadingDialog.dismissDialog();
                    }
                })
                .addOnFailureListener(signInError -> {


                    auth.createUserWithEmailAndPassword(email, EVALUATOR_PASSWORD)
                            .addOnSuccessListener(result -> {
                                FirebaseUser user = result.getUser();
                                if (user != null) {
                                    makeUserEvaluator(user);
                                } else {
                                    loadingDialog.dismissDialog();
                                }
                            })
                            .addOnFailureListener(createError -> {

                                if (createError.getMessage() != null &&
                                        createError.getMessage().toLowerCase().contains("already in use")) {

                                    auth.signInWithEmailAndPassword(email, EVALUATOR_PASSWORD)
                                            .addOnSuccessListener(result -> {
                                                FirebaseUser user = result.getUser();
                                                if (user != null) {
                                                    makeUserEvaluator(user);
                                                } else {
                                                    loadingDialog.dismissDialog();
                                                }
                                            })
                                            .addOnFailureListener(finalError -> {
                                                loadingDialog.dismissDialog();
                                                Toast.makeText(this,
                                                        "Login failed. Contact admin.",
                                                        Toast.LENGTH_LONG).show();
                                            });

                                } else {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(this,
                                            "Auth failed: " + createError.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }




    private void makeUserEvaluator(FirebaseUser user) {

        String uid = user.getUid();
        String email = user.getEmail();

        if (email == null) {
            loadingDialog.dismissDialog();
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Generate evaluator ID from email
        String emailPrefix = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();

        String evaluatorId = "EV-GIET-" + emailPrefix;

        // 🔥 IMPORTANT: ALWAYS store email + role in DB
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("role", "evaluator");
        userData.put("evaluatorId", evaluatorId);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .updateChildren(userData)
                .addOnSuccessListener(unused -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(
                            this,
                            "Evaluator access granted\nID: " + evaluatorId,
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent = new Intent(EvaluatorLogin.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this,
                            "DB Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


}
