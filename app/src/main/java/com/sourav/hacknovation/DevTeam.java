package com.sourav.hacknovation;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class DevTeam extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_dev_team);

        ImageView aboutback = findViewById(R.id.aboutback);
        aboutback.setOnClickListener(v ->
        {
            finish();
        });


        setupSocialLink(R.id.souravinsta, "https://www.instagram.com/sourav.pati_?utm_source=ig_web_button_share_sheet&igsh=ZDNlZDc0MzIxNw==");
        setupSocialLink(R.id.souravlink, "https://www.linkedin.com/in/sourav-kumar-pati-aa0833297/?originalSubdomain=in");
        setupSocialLink(R.id.souravgit, "https://github.com/Fpvsourav955");

        setupSocialLink(R.id.anshumaninsta, "https://www.instagram.com/_subrata__d_?igsh=MWp5anVuZjl5c3oz");
        setupSocialLink(R.id.anshumanlink, "https://www.linkedin.com/in/subrata-dhibar/");
        setupSocialLink(R.id.anshumangit, "https://github.com");

        setupSocialLink(R.id.mukeshinsta, "https://www.instagram.com/supriyo_dawn?igsh=N216cjNzbjJtODY=");
        setupSocialLink(R.id.mukeshlink, "https://www.linkedin.com/in/supriyo-dawn");
        setupSocialLink(R.id.mukeshgit, "https://github.com");

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

    /**
     * Sets up a click listener for a button to navigate to a specified activity.
     *
     * @param buttonId    ID of the button
     * @param targetClass Target activity class
     */
    private void setupMoreButton(int buttonId, Class<?> targetClass) {
        findViewById(buttonId).setOnClickListener(v -> navigateToActivity(targetClass));
    }

    /**
     * Sets up a click listener for an ImageView to open a URL in a browser.
     *
     * @param viewId ID of the ImageView
     * @param url    URL to open
     */
    private void setupSocialLink(int viewId, String url) {
        findViewById(viewId).setOnClickListener(v -> openUrl(url));
    }

    /**
     * Opens a URL in the browser.
     *
     * @param url URL to open
     */
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Navigates to the specified activity.
     *
     * @param targetClass Target activity class
     */
    private void navigateToActivity(Class<?> targetClass) {
        Intent intent = new Intent(DevTeam.this, targetClass);
        startActivity(intent);
    }
}
