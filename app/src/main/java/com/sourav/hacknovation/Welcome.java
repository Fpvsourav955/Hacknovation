package com.sourav.hacknovation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class Welcome extends AppCompatActivity {

    private VideoView videoView;
    private TextView tvTitle, tvTagline;
    private AppCompatButton btnGetStarted;

    private static final long TYPE_DELAY = 90;
    private static final long DELETE_DELAY = 60;
    private static final long HOLD_DELAY = 1200;

    private final String TITLE_TEXT = "HACKNOVATION 2.0";
    private final String TAG_TEXT = "Don't Just Innovate, Hacknovate!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("first_time", true)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_welcome);

        videoView = findViewById(R.id.videoView);
        tvTitle = findViewById(R.id.tvTitle);
        tvTagline = findViewById(R.id.tvTagline);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        startTypewriter(tvTitle, "HACKNOVATION 2.0", 400);
        startTypewriter(tvTagline, "Don't Just Innovate, Hacknovate!", 1500);

        playWelcomeVideo();


        startTypewriter(tvTitle, TITLE_TEXT, 300);
        startTypewriter(tvTagline, TAG_TEXT, 1400);

        btnGetStarted.setOnClickListener(v -> {
            prefs.edit().putBoolean("first_time", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
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

    private void playWelcomeVideo() {


        Uri welcomeUri = Uri.parse(
                "android.resource://" + getPackageName() + "/" + R.raw.welcome_video
        );
        videoView.setVideoURI(welcomeUri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            mp.setVolume(0.5f, 0.5f);
            videoView.start();
        });
        videoView.setOnCompletionListener(mp -> playLaunchVideo());
    }

    private void playLaunchVideo() {

        Uri launchUri = Uri.parse(
                "android.resource://" + getPackageName() + "/" + R.raw.launch1
        );
        videoView.setVideoURI(launchUri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0.1f, 0.1f);
            videoView.start();
        });
    }


    private void startTypewriter(TextView textView, String text, long startDelay) {

        Handler handler = new Handler();
        final int[] index = {0};
        final boolean[] deleting = {false};
        final boolean[] cursorVisible = {true};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {


                if (index[0] < 0) index[0] = 0;
                if (index[0] > text.length()) index[0] = text.length();

                String cursor = cursorVisible[0] ? "|" : "";
                cursorVisible[0] = !cursorVisible[0];

                if (!deleting[0]) {

                    textView.setText(text.substring(0, index[0]) + cursor);

                    if (index[0] == text.length()) {
                        deleting[0] = true;
                        handler.postDelayed(this, 1200);
                    } else {
                        index[0]++;
                        handler.postDelayed(this, 90);
                    }

                } else {

                    textView.setText(text.substring(0, index[0]) + cursor);

                    if (index[0] == 0) {
                        deleting[0] = false;
                        handler.postDelayed(this, 600);
                    } else {
                        index[0]--;
                        handler.postDelayed(this, 60);
                    }
                }
            }
        };

        handler.postDelayed(runnable, startDelay);
    }

}
