package com.sourav.hacknovation;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EvaluationActivity extends AppCompatActivity {

    // ─── ROUND CONSTANTS ────────────────────────────────────────────────────────
    private static final String ROUND_1 = "Round 1: Initial Pitch";
    private static final String ROUND_FINAL = "Round 2: Final Round";

    // ─── ROUND 1 categories (max 10 each) ───────────────────────────────────────
    // Innovation, Technicality, UI/UX, Presentation, Feasibility

    // ─── FINAL ROUND categories & max points ────────────────────────────────────
    // Prototype Development /20, Sustainability /15, Business Model /20,
    // Market Potential /15, Impact & Scalability /10  → total 80 per evaluator

    // ─── Views ──────────────────────────────────────────────────────────────────
    TextView tvTeamTitle, tvProjectId, tvEvaluatorName;
    AutoCompleteTextView spinnerRound;
    TextView label6, tvMax6, tvScore6, tvSub6;
    SeekBar sbCat6;
    // Category labels (we update text dynamically)
    TextView label1, label2, label3, label4, label5;
    TextView tvMax1, tvMax2, tvMax3, tvMax4, tvMax5;  // "/ 10" labels

    SeekBar sbCat1, sbCat2, sbCat3, sbCat4, sbCat5;
    TextView tvScore1, tvScore2, tvScore3, tvScore4, tvScore5;

    // Sub-labels
    TextView tvSub1, tvSub2, tvSub3, tvSub4, tvSub5;

    LoadingDialog loadingDialog;

    // ─── Data ───────────────────────────────────────────────────────────────────
    String teamKey, teamName, projectId;
    String selectedRound = ROUND_1;
    String evaluatorUid;          // Firebase Auth UID
    String evaluatorCustomId;     // e.g. "EV-GIET-SONICFOX858"
    String evaluatorDisplayName;  // fetched from DB

    // Final-round max values per category
    private static final int[] FINAL_MAX = {20, 20, 20, 20, 10,10};
    private static final String[] FINAL_LABELS = {
            "Prototype Development",
            "Sustainability",
            "Business Model",
            "Market Potential",
            "Impact & Scalability",
            "Pitch Deck"
    };
    private static final String[] FINAL_SUBS = {
            "Working demo & technical depth",
            "Environmental & social impact",
            "Revenue model & go-to-market",
            "Target audience & growth potential",
            "Long-term reach & societal benefit",
            "Presentation"
    };

    private static final String[] ROUND1_LABELS = {
            "Innovation", "Technicality", "UI/UX", "Presentation", "Feasibility"
    };
    private static final String[] ROUND1_SUBS = {
            "Creativity & Originality",
            "Design & User Experience",
            "Design & User Experience",
            "Pitch & Communication",
            "Market & Scalability"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_evaluation);

        loadingDialog = new LoadingDialog(this);

        // Intent data
        teamKey   = getIntent().getStringExtra("teamKey");
        teamName  = getIntent().getStringExtra("teamName");
        projectId = getIntent().getStringExtra("projectId");
        evaluatorUid = FirebaseAuth.getInstance().getUid();

        if (teamKey == null) {
            Toast.makeText(this, "Team not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRoundSpinner();
        bindAllSeekBars();
        fetchEvaluatorName();   // ← NEW: load name from Firebase

        if (teamName  != null) tvTeamTitle.setText(teamName);
        if (projectId != null) tvProjectId.setText("Problem ID: " + projectId);

        findViewById(R.id.btnSubmitScore).setOnClickListener(v -> saveEvaluation());
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

    // ─────────────────────────────────────────────────────────────────────────────
    // VIEW BINDING
    // ─────────────────────────────────────────────────────────────────────────────

    private void bindViews() {
        tvTeamTitle      = findViewById(R.id.tvTeamTitle);
        tvProjectId      = findViewById(R.id.tvProjectId);
        tvEvaluatorName  = findViewById(R.id.tvEvaluatorName);  // Add this TextView to your XML
        spinnerRound     = findViewById(R.id.spinnerRound);

        label1 = findViewById(R.id.label1);
        label2 = findViewById(R.id.label2);
        label3 = findViewById(R.id.label3);
        label4 = findViewById(R.id.label4);
        label5 = findViewById(R.id.label5);

        // "/ 10" TextViews — add android:id to each in XML (tvMax1..tvMax5)
        tvMax1 = findViewById(R.id.tvMax1);
        tvMax2 = findViewById(R.id.tvMax2);
        tvMax3 = findViewById(R.id.tvMax3);
        tvMax4 = findViewById(R.id.tvMax4);
        tvMax5 = findViewById(R.id.tvMax5);
        label6   = findViewById(R.id.label6);
        tvMax6   = findViewById(R.id.tvMax6);
        tvScore6 = findViewById(R.id.tvpitchdeck);
        tvSub6   = findViewById(R.id.tvSub6);
        sbCat6   = findViewById(R.id.pitchdeckdes);
        sbCat1 = findViewById(R.id.sbInnovation);
        sbCat2 = findViewById(R.id.sbTechnicality);
        sbCat3 = findViewById(R.id.sbUIUX);
        sbCat4 = findViewById(R.id.sbPresentation);
        sbCat5 = findViewById(R.id.sbfeasibility);

        tvScore1 = findViewById(R.id.tvInnovationScore);
        tvScore2 = findViewById(R.id.tvTechnicalityScore);
        tvScore3 = findViewById(R.id.tvUiScore);
        tvScore4 = findViewById(R.id.tvpresentaionScore);
        tvScore5 = findViewById(R.id.tvfeasibilityScore);

        // Sub-label TextViews — add android:id to each in XML (tvSub1..tvSub5)
        tvSub1 = findViewById(R.id.tvSub1);
        tvSub2 = findViewById(R.id.tvSub2);
        tvSub3 = findViewById(R.id.tvSub3);
        tvSub4 = findViewById(R.id.tvSub4);
        tvSub5 = findViewById(R.id.tvSub5);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // EVALUATOR NAME  (fetched from Users/<uid>/evaluatorId → then look up name)
    // ─────────────────────────────────────────────────────────────────────────────

    private void fetchEvaluatorName() {
        if (evaluatorUid == null) {
            android.util.Log.e("EvalActivity", "fetchEvaluatorName: evaluatorUid is NULL");
            return;
        }

        findViewById(R.id.btnSubmitScore).setEnabled(false);
        tvEvaluatorName.setText("Loading evaluator info...");

        android.util.Log.d("EvalActivity", "Fetching evaluator at path: users/" + evaluatorUid);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(evaluatorUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                android.util.Log.d("EvalActivity", "snapshot exists: " + snapshot.exists());
                android.util.Log.d("EvalActivity", "snapshot value: " + snapshot.getValue());

                if (snapshot.exists()) {
                    // Log ALL fields present in this user node for debugging
                    android.util.Log.d("EvalActivity", "=== USER NODE FIELDS ===");
                    for (DataSnapshot child : snapshot.getChildren()) {
                        android.util.Log.d("EvalActivity", "  field: " + child.getKey() + " = " + child.getValue());
                    }

                    evaluatorCustomId = snapshot.child("evaluatorId").getValue(String.class);
                    android.util.Log.d("EvalActivity", "evaluatorId field value: [" + evaluatorCustomId + "]");

                    // role check — make sure this is actually an evaluator account
                    String role = snapshot.child("role").getValue(String.class);
                    android.util.Log.d("EvalActivity", "role field value: [" + role + "]");

                    String name = snapshot.child("name").getValue(String.class);
                    android.util.Log.d("EvalActivity", "name field value: [" + name + "]");

                    // If no "name" field, fall back to evaluatorId
                    if (name == null || name.trim().isEmpty()) {
                        name = evaluatorCustomId;
                    }
                    evaluatorDisplayName = name;

                    if (evaluatorCustomId != null && !evaluatorCustomId.isEmpty()) {
                        // ✅ Success — evaluatorId found, enable submit
                        tvEvaluatorName.setText("Evaluator: " + name);
                        findViewById(R.id.btnSubmitScore).setEnabled(true);
                    } else {
                        // evaluatorId field is missing for this user node
                        android.util.Log.e("EvalActivity",
                                "evaluatorId field is NULL or empty for uid=" + evaluatorUid
                                        + ". Add evaluatorId field to this user in Firebase console.");
                        tvEvaluatorName.setText("No evaluatorId set for this account");
                        Toast.makeText(EvaluationActivity.this,
                                "This account has no evaluatorId. Ask admin to add it in Firebase.",
                                Toast.LENGTH_LONG).show();
                        // Keep button disabled — cannot save without a valid node key
                    }
                } else {
                    // The uid from FirebaseAuth does not exist under users/ collection
                    android.util.Log.e("EvalActivity",
                            "No user node found at users/" + evaluatorUid
                                    + ". This UID is not registered in the database.");
                    tvEvaluatorName.setText("Account not found in database");
                    Toast.makeText(EvaluationActivity.this,
                            "Your account is not in the database. Contact admin.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                android.util.Log.e("EvalActivity", "Firebase read cancelled: " + error.getMessage());
                tvEvaluatorName.setText("Evaluator: (load failed)");
                Toast.makeText(EvaluationActivity.this,
                        "DB error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                // Do NOT enable submit — evaluatorCustomId is still null
            }
        });
    }


    private void setupRoundSpinner() {
        String[] rounds = {ROUND_1, ROUND_FINAL};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, rounds);
        spinnerRound.setAdapter(adapter);
        spinnerRound.setKeyListener(null);
        spinnerRound.setText(ROUND_FINAL, false);  // ← changed from ROUND_1 to ROUND_FINAL

        spinnerRound.setOnItemClickListener((parent, view, position, id) -> {
            selectedRound = parent.getItemAtPosition(position).toString();
            updateCategoryUI();
        });
        spinnerRound.setOnClickListener(v -> spinnerRound.showDropDown());

        selectedRound = ROUND_FINAL;  // ← add this line so the variable matches
        updateCategoryUI();
    }
    /**
     * Switches all 5 category labels, sub-labels, and SeekBar maxes
     * depending on the selected round.
     */
    private void updateCategoryUI() {
        boolean isFinal = ROUND_FINAL.equals(selectedRound);

        String[] labels = isFinal ? FINAL_LABELS : ROUND1_LABELS;
        String[] subs   = isFinal ? FINAL_SUBS   : ROUND1_SUBS;
        int[]    maxes  = isFinal ? FINAL_MAX     : new int[]{10, 10, 10, 10, 10};

        label1.setText(labels[0]); label2.setText(labels[1]);
        label3.setText(labels[2]); label4.setText(labels[3]);
        label5.setText(labels[4]);

        if (tvSub1 != null) {
            tvSub1.setText(subs[0]); tvSub2.setText(subs[1]);
            tvSub3.setText(subs[2]); tvSub4.setText(subs[3]);
            tvSub5.setText(subs[4]);
        }

        setSeekBarMax(sbCat1, tvScore1, tvMax1, maxes[0]);
        setSeekBarMax(sbCat2, tvScore2, tvMax2, maxes[1]);
        setSeekBarMax(sbCat3, tvScore3, tvMax3, maxes[2]);
        setSeekBarMax(sbCat4, tvScore4, tvMax4, maxes[3]);
        setSeekBarMax(sbCat5, tvScore5, tvMax5, maxes[4]);

        // ── 6th category (Pitch Deck — Final only) ──
        if (isFinal) {
            if (label6  != null) label6.setText(labels[5]);
            if (tvSub6  != null) tvSub6.setText(subs[5]);
            setSeekBarMax(sbCat6, tvScore6, tvMax6, maxes[5]);
            if (sbCat6  != null) sbCat6.setVisibility(android.view.View.VISIBLE);
            if (label6  != null) label6.setVisibility(android.view.View.VISIBLE);
        } else {
            // Hide pitch deck row in Round 1
            if (sbCat6  != null) sbCat6.setVisibility(android.view.View.GONE);
            if (label6  != null) label6.setVisibility(android.view.View.GONE);
            if (tvSub6  != null) tvSub6.setVisibility(android.view.View.GONE);
            if (tvMax6  != null) tvMax6.setVisibility(android.view.View.GONE);
            if (tvScore6 != null) tvScore6.setVisibility(android.view.View.GONE);
        }
    }

    private void setSeekBarMax(SeekBar sb, TextView scoreView, TextView maxView, int max) {
        sb.setMax(max);
        sb.setProgress(0);
        scoreView.setText("0");
        if (maxView != null) maxView.setText("/ " + max);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SEEKBAR LISTENERS
    // ─────────────────────────────────────────────────────────────────────────────

    private void bindAllSeekBars() {
        bindSeekBar(sbCat1, tvScore1);
        bindSeekBar(sbCat2, tvScore2);
        bindSeekBar(sbCat3, tvScore3);
        bindSeekBar(sbCat4, tvScore4);
        bindSeekBar(sbCat5, tvScore5);
        bindSeekBar(sbCat6, tvScore6);
    }

    private void bindSeekBar(SeekBar seekBar, TextView scoreText) {
        scoreText.setText(String.valueOf(seekBar.getProgress()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                scoreText.setText(String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SAVE EVALUATION
    // Stores this evaluator's scores, then reads ALL evaluators in this round
    // and writes the aggregate total under the team node.
    // ─────────────────────────────────────────────────────────────────────────────

    private void saveEvaluation() {
        if (evaluatorUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        if (evaluatorCustomId == null || evaluatorCustomId.isEmpty()) {
            Toast.makeText(this, "Evaluator ID not ready. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nodeKey = evaluatorCustomId; // e.g. "EV-GIET-SOURAVPATI"
        loadingDialog.startLoadingDiloag();

        int c1 = sbCat1.getProgress();
        int c2 = sbCat2.getProgress();
        int c3 = sbCat3.getProgress();
        int c4 = sbCat4.getProgress();
        int c5 = sbCat5.getProgress();
        int c6 = sbCat6 != null ? sbCat6.getProgress() : 0;  // ← added
        int myTotal = c1 + c2 + c3 + c4 + c5 + c6;

        Map<String, Object> data = new HashMap<>();
        boolean isFinal = ROUND_FINAL.equals(selectedRound);

        if (isFinal) {
            data.put("prototypeDevelopment", c1);
            data.put("sustainability",        c2);
            data.put("businessModel",         c3);
            data.put("marketPotential",       c4);
            data.put("impactAndScalability",  c5);
            data.put("pitchDeck",             c6);
        } else {
            data.put("innovation",   c1);
            data.put("technicality", c2);
            data.put("uiux",         c3);
            data.put("presentation", c4);
            data.put("feasibility",  c5);
        }

        data.put("evaluatorName", evaluatorDisplayName != null ? evaluatorDisplayName : nodeKey);
        data.put("individualTotal", myTotal);
        data.put("timestamp", System.currentTimeMillis());

        // References
        DatabaseReference teamRef = FirebaseDatabase.getInstance()
                .getReference("Teams")
                .child(teamKey);

        DatabaseReference evaluationsRef = teamRef.child("Evaluations");
        DatabaseReference roundRef       = evaluationsRef.child(selectedRound);
        DatabaseReference evalRef        = roundRef.child(nodeKey);

        // Step 1: Save this evaluator's scores for this round
        evalRef.setValue(data).addOnSuccessListener(unused -> {

            // Step 2: Read the ENTIRE Evaluations node (all rounds, all evaluators)
            // and sum every individualTotal across every round and every evaluator
            // → write ONE grand "total" directly on the Team node
            evaluationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot evaluationsSnap) {
                    long grandTotal = 0;

                    // Loop every round ("Round 1: Initial Pitch", "Round 2: Final Round", ...)
                    for (DataSnapshot roundSnap : evaluationsSnap.getChildren()) {
                        // Loop every evaluator node inside that round
                        for (DataSnapshot evaluatorSnap : roundSnap.getChildren()) {
                            // Skip the "aggregateTotal" field node (it's not an evaluator)
                            if (evaluatorSnap.getKey() != null
                                    && evaluatorSnap.getKey().equals("aggregateTotal")) continue;

                            Long indTotal = evaluatorSnap.child("individualTotal").getValue(Long.class);
                            if (indTotal != null) grandTotal += indTotal;
                        }
                    }

                    // Write grand total to Teams/<teamKey>/total
                    // This single field always reflects SUM of ALL evaluators across ALL rounds
                    Map<String, Object> totalMap = new HashMap<>();
                    totalMap.put("total", grandTotal);
                    teamRef.updateChildren(totalMap)
                            .addOnCompleteListener(task -> {
                                loadingDialog.dismissDialog();
                                showSuccessDialog();
                            });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    loadingDialog.dismissDialog();
                    showSuccessDialog();
                }
            });

        }).addOnFailureListener(e -> {
            loadingDialog.dismissDialog();
            Toast.makeText(this, "Failed to save evaluation", Toast.LENGTH_SHORT).show();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SUCCESS DIALOG
    // ─────────────────────────────────────────────────────────────────────────────

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            finish();
        }, 2000);
    }


}