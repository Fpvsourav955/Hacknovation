package com.sourav.hacknovation;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class RanksFragment extends Fragment {

    private static final int UNLOCK_YEAR   = 2026;
    private static final int UNLOCK_MONTH  = Calendar.FEBRUARY;
    private static final int UNLOCK_DAY    = 21;
    private static final int UNLOCK_HOUR   = 19; // 7 PM
    private static final int UNLOCK_MINUTE = 0;

    CardView countdownCard;
    TextView tvDays, tvHours, tvMinutes, tvSeconds;

    CountDownTimer countDownTimer;

    public RanksFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ranks, container, false);

        countdownCard = view.findViewById(R.id.countdownCard);
        tvDays        = view.findViewById(R.id.tvDays);
        tvHours       = view.findViewById(R.id.tvHours);
        tvMinutes     = view.findViewById(R.id.tvMinutes);
        tvSeconds     = view.findViewById(R.id.tvSeconds);

        checkUnlockTime();

        return view;
    }

    private void checkUnlockTime() {
        Calendar unlock = Calendar.getInstance();
        unlock.set(UNLOCK_YEAR, UNLOCK_MONTH, UNLOCK_DAY,
                UNLOCK_HOUR, UNLOCK_MINUTE, 0);
        unlock.set(Calendar.MILLISECOND, 0);

        long unlockMillis = unlock.getTimeInMillis();
        long now = System.currentTimeMillis();

        if (now >= unlockMillis) {
            countdownCard.setVisibility(View.GONE);

            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "🏆 Evaluation Results Released Soon",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            countdownCard.setVisibility(View.VISIBLE);
            startCountdown(unlockMillis - now);
        }
    }

    private void startCountdown(long millis) {

        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long ms) {

                long totalSeconds = ms / 1000;
                long days    = totalSeconds / (24 * 3600);
                totalSeconds %= (24 * 3600);
                long hours   = totalSeconds / 3600;
                totalSeconds %= 3600;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;

                tvDays.setText(String.valueOf(days));
                tvHours.setText(String.valueOf(hours));
                tvMinutes.setText(String.valueOf(minutes));
                tvSeconds.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {

                countdownCard.setVisibility(View.GONE);

                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "🏆 Evaluation Results Released!",
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        countDownTimer.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}