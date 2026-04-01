package com.sourav.hacknovation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageSlider imageSlider;
    private DatabaseReference bannerRef;

    private TextView tvDays, tvHours, tvMinutes, tvSeconds;
    private TextView tvCountdownLabel;
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private LinearLayout admin1;

    // ── Countdown targets ────────────────────────────────────────────────────────
    // Phase 1: Feb 21, 2026 at 8:00 AM  → event starts
    // Phase 2: Feb 22, 2026 at 6:00 PM  → event ends
    private static final long TARGET_1_MILLIS;
    private static final long TARGET_2_MILLIS;

    static {
        Calendar c1 = Calendar.getInstance();
        c1.set(2026, Calendar.FEBRUARY, 21, 8, 0, 0);
        c1.set(Calendar.MILLISECOND, 0);
        TARGET_1_MILLIS = c1.getTimeInMillis();

        Calendar c2 = Calendar.getInstance();
        c2.set(2026, Calendar.FEBRUARY, 22, 18, 0, 0);
        c2.set(Calendar.MILLISECOND, 0);
        TARGET_2_MILLIS = c2.getTimeInMillis();
    }

    private final List<SliderController> sliders = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageSlider = view.findViewById(R.id.slider);
        bannerRef = FirebaseDatabase.getInstance().getReference("SliderImages");
        loadBannerImages();

        tvDays    = view.findViewById(R.id.tvDays);
        tvHours   = view.findViewById(R.id.tvHours);
        tvMinutes = view.findViewById(R.id.tvMinutes);
        tvSeconds = view.findViewById(R.id.tvSeconds);

        tvCountdownLabel = view.findViewById(R.id.cardcount);

        admin1 = view.findViewById(R.id.admin1);
        if (admin1 != null) {
            admin1.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Only Admin Can Access!", Toast.LENGTH_SHORT).show()
            );
        }

        startChainedCountdown();
        initSliders(view);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CHAINED COUNTDOWN
    // Phase 1 → counts down to Feb 21 8:00 AM ("Hackathon starts in")
    // Phase 2 → immediately starts counting down to Feb 22 6:00 PM ("Hackathon ends in")
    // After Phase 2 ends → shows 00:00:00:00
    // ─────────────────────────────────────────────────────────────────────────────
    @SuppressLint("DefaultLocale")
    private void startChainedCountdown() {
        countdownHandler = new Handler(Looper.getMainLooper());

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;

                long now = System.currentTimeMillis();

                long targetMillis;

                if (now < TARGET_1_MILLIS) {

                    targetMillis = TARGET_1_MILLIS;
                    tvCountdownLabel.setText("Hackathon Starts In");

                } else if (now < TARGET_2_MILLIS) {
                    targetMillis = TARGET_2_MILLIS;
                    tvCountdownLabel.setText("Hackathon Started ..");
                } else {

                    tvDays.setText("00");
                    tvHours.setText("00");
                    tvMinutes.setText("00");
                    tvSeconds.setText("00");
                    tvCountdownLabel.setText("Hackathon Completed!");
                    return;
                }

                long diff = targetMillis - now;
                long days    = diff / (1000 * 60 * 60 * 24);
                long hours   = (diff / (1000 * 60 * 60)) % 24;
                long minutes = (diff / (1000 * 60)) % 60;
                long seconds = (diff / 1000) % 60;

                tvDays.setText(String.format("%02d", days));
                tvHours.setText(String.format("%02d", hours));
                tvMinutes.setText(String.format("%02d", minutes));
                tvSeconds.setText(String.format("%02d", seconds));

                // Tick every second — same runnable handles both phases automatically
                countdownHandler.postDelayed(this, 1000);
            }
        };

        countdownHandler.post(countdownRunnable);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // BANNER & SLIDERS (unchanged)
    // ─────────────────────────────────────────────────────────────────────────────

    private void loadBannerImages() {
        bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                ArrayList<SlideModel> models = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null && !url.isEmpty()) {
                        models.add(new SlideModel(url, ScaleTypes.CENTER_CROP));
                    }
                }
                if (!models.isEmpty()) imageSlider.setImageList(models);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) Toast.makeText(getContext(), "Banner load failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initSliders(View view) {
        int[] viewPagerIds = {
                R.id.viewPagerSlider,
                R.id.viewPagerSlider1,
                R.id.viewPagerSlider2,
                R.id.viewPagerSlider3
        };
        String[] firebaseNodes = {
                "SliderImages1",
                "SliderImages2",
                "SliderImages3",
                "SliderImage4"
        };
        for (int i = 0; i < viewPagerIds.length; i++) {
            ViewPager2 vp = view.findViewById(viewPagerIds[i]);
            SliderController controller = new SliderController(vp, requireContext());
            sliders.add(controller);
            loadSliderData(firebaseNodes[i], controller);
        }
    }

    private void loadSliderData(String node, SliderController controller) {
        FirebaseDatabase.getInstance()
                .getReference(node)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        controller.items.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String url = data.getValue(String.class);
                            if (url != null && !url.isEmpty()) {
                                controller.items.add(new SlideItem(url));
                            }
                        }
                        controller.adapter.notifyDataSetChanged();
                        controller.startAutoScroll();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeFragment", error.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (SliderController controller : sliders) controller.stop();
        if (countdownHandler != null) countdownHandler.removeCallbacks(countdownRunnable);
    }


    static class SliderController {
        ViewPager2 viewPager;
        SlideAdapter adapter;
        List<SlideItem> items = new ArrayList<>();
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable;

        SliderController(ViewPager2 vp, android.content.Context context) {
            viewPager = vp;
            adapter = new SlideAdapter(context, items);
            viewPager.setAdapter(adapter);
            setupViewPager();
        }

        private void setupViewPager() {
            viewPager.setClipToPadding(false);
            viewPager.setClipChildren(false);
            viewPager.setOffscreenPageLimit(3);
            viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            CompositePageTransformer transformer = new CompositePageTransformer();
            transformer.addTransformer(new MarginPageTransformer(40));
            transformer.addTransformer((page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            });
            viewPager.setPageTransformer(transformer);
        }

        void startAutoScroll() {
            stop();
            runnable = () -> {
                if (items.isEmpty()) return;
                int next = (viewPager.getCurrentItem() + 1) % items.size();
                viewPager.setCurrentItem(next, true);
                handler.postDelayed(runnable, 3000);
            };
            handler.postDelayed(runnable, 3000);
        }

        void stop() {
            if (runnable != null) handler.removeCallbacks(runnable);
        }
    }
}