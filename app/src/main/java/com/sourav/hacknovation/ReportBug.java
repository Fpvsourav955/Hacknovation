package com.sourav.hacknovation;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ReportBug extends AppCompatActivity {
    EditText etUserName, etTitle, etDescription;
    LinearLayout btnUpload;
    LoadingDialog loadingDialog;

    Uri imageUri;
    TextView txtUploadStatus;

    DatabaseReference bugRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_report_bug);
        loadingDialog = new LoadingDialog(this);
        txtUploadStatus = findViewById(R.id.txtUploadStatus);

        etUserName = findViewById(R.id.etUserName);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnUpload = findViewById(R.id.btnUpload);

        bugRef = FirebaseDatabase.getInstance().getReference("bug_reports");
        storageRef = FirebaseStorage.getInstance().getReference("bug_screenshots");
        btnUpload.setOnClickListener(v -> pickImage());

        findViewById(R.id.btnSubmitBug).setOnClickListener(v -> submitBug());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        LinearLayout btnBack= findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v->{
            finish();
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

    ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;

                    String fileName = getFileName(uri);
                    txtUploadStatus.setText(fileName);
                    txtUploadStatus.setTextColor(getColor(R.color.primary));
                }

            });

    private void pickImage() {
        imagePicker.launch("image/*");
    }


    private void submitBug() {

        String name = etUserName.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (name.isEmpty() || title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.startLoadingDiloag();

        if (imageUri != null) {
            uploadImageAndSave(name, title, desc);
        } else {
            saveBug(name, title, desc, null);
        }
    }

    private void uploadImageAndSave(String name, String title, String desc) {

        String key = bugRef.push().getKey();
        StorageReference imgRef = storageRef.child(key + ".jpg");

        imgRef.putFile(imageUri)
                .continueWithTask(task -> imgRef.getDownloadUrl())
                .addOnSuccessListener(uri ->
                        saveBug(name, title, desc, uri.toString()))
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveBug(String name, String title, String desc, String imageUrl) {

        String key = bugRef.push().getKey();

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("title", title);
        map.put("description", desc);
        map.put("imageUrl", imageUrl);
        map.put("timestamp", System.currentTimeMillis());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            map.put("userUid", FirebaseAuth.getInstance().getUid());
        }

        bugRef.child(key).setValue(map)
                .addOnSuccessListener(unused -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this, "Bug reported successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private String getFileName(Uri uri) {
        String result = "Selected Image";

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        } else {
            result = uri.getLastPathSegment();
        }
        return result;
    }

}
