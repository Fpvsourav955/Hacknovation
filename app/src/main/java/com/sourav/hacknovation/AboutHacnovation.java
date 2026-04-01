package com.sourav.hacknovation;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

public class AboutHacnovation extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private RoundedImageView profileImage1, profileImage2, profileImage3, profileImage4, profileImage5;
    private LoadingDialog loadingDialog;
    private LinearLayout btnBack;
    private int imagesToLoad = 5;
    private int imagesLoaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_about_hacnovation);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadingDiloag();

        initializeViews();
        setupClickListeners();
        loadImagesFromFirebase();

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

    private void initializeViews() {
        profileImage1 = findViewById(R.id.profileImage1);
        profileImage2 = findViewById(R.id.profileImage2);
        profileImage3 = findViewById(R.id.profileImage3);
        profileImage4 = findViewById(R.id.profileImage4);
        profileImage5 = findViewById(R.id.profileImage5);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadImagesFromFirebase() {
        DatabaseReference aboutImageRef = databaseReference.child("aboutImage");

        aboutImageRef.child("landscape").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String landscapeImageUrl = snapshot.child("image1").getValue(String.class);
                    if (landscapeImageUrl != null && !landscapeImageUrl.isEmpty()) {
                        loadImageWithGlide(landscapeImageUrl, profileImage3);
                    } else {
                        checkLoadingComplete();
                    }
                } else {
                    checkLoadingComplete();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkLoadingComplete();
                Toast.makeText(AboutHacnovation.this,
                        "Failed to load landscape image", Toast.LENGTH_SHORT).show();
            }
        });

        aboutImageRef.child("portrait").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String image1Url = snapshot.child("image1").getValue(String.class);
                    String image2Url = snapshot.child("image2").getValue(String.class);
                    String image4Url = snapshot.child("image3").getValue(String.class);
                    String image5Url = snapshot.child("image4").getValue(String.class);

                    if (image1Url != null && !image1Url.isEmpty()) {
                        loadImageWithGlide(image1Url, profileImage1);
                    } else {
                        checkLoadingComplete();
                    }

                    if (image2Url != null && !image2Url.isEmpty()) {
                        loadImageWithGlide(image2Url, profileImage2);
                    } else {
                        checkLoadingComplete();
                    }

                    if (image4Url != null && !image4Url.isEmpty()) {
                        loadImageWithGlide(image4Url, profileImage4);
                    } else {
                        checkLoadingComplete();
                    }

                    if (image5Url != null && !image5Url.isEmpty()) {
                        loadImageWithGlide(image5Url, profileImage5);
                    } else {
                        checkLoadingComplete();
                    }

                } else {
                    checkLoadingComplete();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkLoadingComplete();
                Toast.makeText(AboutHacnovation.this,
                        "Failed to load portrait images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImageWithGlide(String imageUrl, RoundedImageView imageView) {
        if (imageView == null) {
            checkLoadingComplete();
            return;
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.gradient_background)
                .error(R.drawable.gradient_background)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        checkLoadingComplete();
    }

    private void checkLoadingComplete() {
        imagesLoaded++;
        if (imagesLoaded >= imagesToLoad) {
            if (loadingDialog != null) {
                loadingDialog.dismissDialog();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismissDialog();
        }
        super.onDestroy();
    }
}
