package com.sourav.hacknovation;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    private SmoothBottomBar studentBottomBar;
    private SmoothBottomBar evaluatorBottomBar;
    private SmoothBottomBar adminBottomBar;
    private AppUpdateManager appUpdateManager;
    private FirebaseAuth auth;
    private DatabaseReference db;
    private LoadingDialog loadingDialog;
    private UserRole currentUserRole;
    private boolean fragmentLoaded = false;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingDialog = new LoadingDialog(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        checkForInAppUpdate();
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Menu menu = navigationView.getMenu();
        MenuItem logoutItem = menu.findItem(R.id.logout);
        if (logoutItem != null) {
            SpannableString spanString = new SpannableString(logoutItem.getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.RED), 0, spanString.length(), 0);
            logoutItem.setTitle(spanString);

            if (logoutItem.getIcon() != null) {
                Drawable icon = logoutItem.getIcon().mutate();
                DrawableCompat.setTint(icon, Color.RED);
                logoutItem.setIcon(icon);
            }
        }

        navigationView.setNavigationItemSelectedListener(this::handleDrawerItemClick);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        View headerView = navigationView.getHeaderView(0);
        TextView profile_name = headerView.findViewById(R.id.Profile_name);
        TextView profile_email = headerView.findViewById(R.id.Profile_email);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            profile_name.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown Name");
            profile_email.setText(user.getEmail() != null ? user.getEmail() : "No Email Available");
        } else {
            Toast.makeText(this, "No user signed in!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        studentBottomBar = findViewById(R.id.bottomBarStudent);
        evaluatorBottomBar = findViewById(R.id.bottomBarEvaluator);
        adminBottomBar = findViewById(R.id.bottomBarAdmin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
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

        loadingDialog.startLoadingDiloag();
        checkUserRole(user.getUid());
    }

    private boolean handleDrawerItemClick(MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.Faculity1) {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this, Faculity.class));

        } else if (id == R.id.Team) {
            startActivity(new Intent(this, Members.class));

        } else if (id == R.id.Devteam) {
            startActivity(new Intent(this, DevTeam.class));

        } else if (id == R.id.AboutHacknovation) {
            startActivity(new Intent(this, AboutHacnovation.class));

        } else if (id == R.id.ReportBug) {
            startActivity(new Intent(this, ReportBug.class));

        } else if (id == R.id.Feedback) {
            startActivity(new Intent(this, Feedback.class));

        } else if (id == R.id.contactus) {
            startActivity(new Intent(this, ContactUs.class));

        } else if (id == R.id.share) {

            String shareLink = "https://play.google.com/store/apps/details?id=com.sourav.hacknovation";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hacknovation App");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Download Hacknovation App from Play Store:\n" + shareLink);

            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
        else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    private void checkUserRole(String uid) {
        db.child("Admin").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            setupAfterRoleDetected(UserRole.ADMIN);
                        } else {
                            checkUserNode(uid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingDialog.dismissDialog();
                        showError();
                    }
                });
    }

    private void checkUserNode(String uid) {
        db.child("users").child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);

                        if ("evaluator".equalsIgnoreCase(role)) {
                            setupAfterRoleDetected(UserRole.EVALUATOR);
                        } else {
                            setupAfterRoleDetected(UserRole.STUDENT);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingDialog.dismissDialog();
                        showError();
                    }
                });
    }

    private void setupAfterRoleDetected(UserRole role) {
        currentUserRole = role;

        showBottomBarForRole(role);
        setupBottomBarListener(role);
        if (!fragmentLoaded && !getSupportFragmentManager().isStateSaved()) {
            loadFragment(new HomeFragment());
            fragmentLoaded = true;
        }

        loadingDialog.dismissDialog();
    }

    private void showBottomBarForRole(UserRole role) {
        studentBottomBar.setVisibility(View.GONE);
        evaluatorBottomBar.setVisibility(View.GONE);
        adminBottomBar.setVisibility(View.GONE);

        switch (role) {
            case ADMIN:
                adminBottomBar.setVisibility(View.VISIBLE);
                break;
            case EVALUATOR:
                evaluatorBottomBar.setVisibility(View.VISIBLE);
                break;
            case STUDENT:
                studentBottomBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupBottomBarListener(UserRole role) {
        OnItemSelectedListener listener = new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int pos) {
                switch (currentUserRole) {
                    case ADMIN:
                        handleAdminNav(pos);
                        break;
                    case EVALUATOR:
                        handleEvaluatorNav(pos);
                        break;
                    case STUDENT:
                        handleStudentNav(pos);
                        break;
                }
                return true;
            }
        };

        switch (role) {
            case ADMIN:
                adminBottomBar.setOnItemSelectedListener(listener);
                break;
            case EVALUATOR:
                evaluatorBottomBar.setOnItemSelectedListener(listener);
                break;
            case STUDENT:
                studentBottomBar.setOnItemSelectedListener(listener);
                break;
        }
    }

    private void handleStudentNav(int pos) {
        switch (pos) {
            case 0: loadFragment(new HomeFragment()); break;
            case 1: loadFragment(new RanksFragment()); break;
            case 2: loadFragment(new ActivityFragment()); break;
            case 3: loadFragment(new ProfileFragment()); break;
        }
    }

    private void handleEvaluatorNav(int pos) {
        switch (pos) {
            case 0: loadFragment(new HomeFragment()); break;
            case 1: loadFragment(new RanksFragment()); break;
            case 2: loadFragment(new TeamsFragment()); break;
            case 3: loadFragment(new ActivityFragment()); break;
        }
    }

    private void handleAdminNav(int pos) {
        switch (pos) {
            case 0: loadFragment(new HomeFragment()); break;
            case 1: loadFragment(new RanksFragment()); break;
            case 2: loadFragment(new AddTeamsFragment()); break;
            case 3: loadFragment(new ActivityFragment()); break;
        }
    }
    private void loadFragment(Fragment fragment) {

        if (isFinishing() || isDestroyed()) return;

        if (getSupportFragmentManager().isStateSaved()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void showError() {
        Toast.makeText(this, "Unable to determine user role", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        toggle.syncState();
    }
    private void checkForInAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build().appUpdateType(),
                            this,
                            100);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Update check failed", Toast.LENGTH_SHORT).show());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode != RESULT_OK) {
            Toast.makeText(this, "Update canceled", Toast.LENGTH_SHORT).show();
        }


    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }
}