package com.sourav.hacknovation;



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private LoadingDialog loadingDialog;

    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private final ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            try {
                                SignInCredential credential =
                                        oneTapClient.getSignInCredentialFromIntent(result.getData());

                                String idToken = credential.getGoogleIdToken();
                                if (idToken != null) {
                                    firebaseAuthWithGoogle(idToken);
                                } else {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(this, "No ID token received", Toast.LENGTH_SHORT).show();
                                }

                            } catch (ApiException e) {
                                loadingDialog.dismissDialog();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loadingDialog.dismissDialog();
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);
        loadingDialog = new LoadingDialog(this);
        AppCompatButton google_btn = findViewById(R.id.google_btn);
        TextView btnAdminLogin =findViewById(R.id.btnAdminLogin);
        btnAdminLogin.setOnClickListener(v->{
            Intent intent = new Intent(LoginActivity.this, EvaluatorLogin.class);
            startActivity(intent);
        });

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    101
            );
        }

        String webClientId = getString(R.string.web_client_id);
        if (TextUtils.isEmpty(webClientId)) {
            Toast.makeText(this, "Web Client ID missing", Toast.LENGTH_LONG).show();
            return;
        }

        oneTapClient = Identity.getSignInClient(this);

        signInRequest =
                BeginSignInRequest.builder()
                        .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                        .setSupported(true)
                                        .setServerClientId(webClientId)
                                        .setFilterByAuthorizedAccounts(false)
                                        .build()
                        )
                        .build();

        google_btn.setOnClickListener(v -> {
            loadingDialog.startLoadingDiloag();
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(result -> {
                        IntentSenderRequest request =
                                new IntentSenderRequest.Builder(
                                        result.getPendingIntent().getIntentSender()
                                ).build();
                        googleSignInLauncher.launch(request);
                    })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismissDialog();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


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

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null || user.getEmail() == null) {
                            loadingDialog.dismissDialog();
                            return;
                        }

                        String email = user.getEmail();


                        String rollNo = "";
                        String fullName = "User";
                        String profileImage = "";
                        String providerUsed = "unknown";

                        for (var profile : user.getProviderData()) {
                            if ("google.com".equals(profile.getProviderId())) {
                                providerUsed = "google";


                                fullName = profile.getDisplayName() != null
                                        ? profile.getDisplayName()
                                        : "User";

                                if (profile.getPhotoUrl() != null) {
                                    profileImage = profile.getPhotoUrl().toString();
                                }
                                break;
                            }
                        }

                        if (email.endsWith("@giet.edu") && email.contains(".")) {
                            try {
                                String beforeAt = email.substring(0, email.indexOf('@'));
                                String[] parts = beforeAt.split("\\.");

                                if (parts.length >= 2) {
                                    rollNo = parts[0];

                                    if ("User".equals(fullName)) {
                                        fullName = capitalizeWords(parts[1]);
                                    }
                                }
                            } catch (Exception ignored) {}
                        }


                        HashMap<String, Object> map = new HashMap<>();
                        map.put("uid", user.getUid());
                        map.put("email", email);
                        map.put("rollNo", rollNo);
                        map.put("name", fullName);
                        map.put("profileImage", profileImage);
                        map.put("role", "student");
                        map.put("provider", providerUsed);

                        database.getReference()
                                .child("users")
                                .child(user.getUid())
                                .updateChildren(map);

                        sendLoginNotification();
                        loadingDialog.dismissDialog();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();


                    } else {
                        loadingDialog.dismissDialog();
                        Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        builder.append(Character.toUpperCase(input.charAt(0)));

        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append(" ");
            }
            builder.append(c);
        }
        return builder.toString();
    }


    private void sendLoginNotification() {
        String channelId = "LOGIN_CHANNEL";

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel =
                new NotificationChannel(
                        channelId,
                        "Login Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.hackimg1)
                        .setContentTitle("Welcome to Hacknovation 2.0")
                        .setContentText("✅ Login successful. Let’s innovate!")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(1, builder.build());
    }
}