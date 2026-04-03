package com.mit.smartcampuscanteen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private static final String KITCHEN_PASSWORD = "kitchen123";
    private static final String COUNTER_PASSWORD  = "counter123";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private LinearLayout panelStudent, panelStaff;
    private TextView tabStudentLogin, tabStaffLogin;
    private TextView tvSelectedRole;
    private EditText etGuestName, etStaffPassword;
    private CardView btnGoogleSignIn, cardRoleKitchen, cardRoleCounter;
    private Button btnGuestLogin, btnStaffLogin;

    private String selectedStaffRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        panelStudent    = findViewById(R.id.panel_student);
        panelStaff      = findViewById(R.id.panel_staff);
        tabStudentLogin = findViewById(R.id.tab_student_login);
        tabStaffLogin   = findViewById(R.id.tab_staff_login);
        tvSelectedRole  = findViewById(R.id.tv_selected_role);
        etGuestName     = findViewById(R.id.et_guest_name);
        etStaffPassword = findViewById(R.id.et_staff_password);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        cardRoleKitchen = findViewById(R.id.card_role_kitchen);
        cardRoleCounter = findViewById(R.id.card_role_counter);
        btnGuestLogin   = findViewById(R.id.btn_guest_login);
        btnStaffLogin   = findViewById(R.id.btn_staff_login);

        tabStudentLogin.setOnClickListener(v -> showPanel(true));
        tabStaffLogin.setOnClickListener(v   -> showPanel(false));

        btnGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());

        btnGuestLogin.setOnClickListener(v -> {
            String name = etGuestName.getText().toString().trim();
            if (name.isEmpty()) {
                // CHANGED: Hindi → English
                etGuestName.setError("Please enter your name!");
                etGuestName.requestFocus();
                return;
            }
            saveUserName(name);
            goTo(StudentActivity.class);
        });

        cardRoleKitchen.setOnClickListener(v -> selectRole("Kitchen"));
        cardRoleCounter.setOnClickListener(v -> selectRole("Counter"));

        btnStaffLogin.setOnClickListener(v -> verifyStaffPassword());

        checkAlreadyLoggedIn();
    }

    // ===== ALREADY LOGGED IN CHECK =====
    private void checkAlreadyLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName() != null
                    ? currentUser.getDisplayName() : "";
            saveUserName(name);

            if (!name.isEmpty()) {
                // CHANGED: Hindi → English
                Toast.makeText(this,
                        "Welcome back, " + name + "! Please select your role.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ===== PANEL SWITCH =====
    private void showPanel(boolean showStudent) {
        if (showStudent) {
            panelStudent.setVisibility(View.VISIBLE);
            panelStaff.setVisibility(View.GONE);
            tabStudentLogin.setTextColor(0xFFFF6B35);
            tabStudentLogin.setBackgroundResource(R.drawable.tab_active_indicator);
            tabStaffLogin.setTextColor(0xFF999999);
            tabStaffLogin.setBackgroundColor(0x00000000);
        } else {
            panelStudent.setVisibility(View.GONE);
            panelStaff.setVisibility(View.VISIBLE);
            tabStaffLogin.setTextColor(0xFFFF6B35);
            tabStaffLogin.setBackgroundResource(R.drawable.tab_active_indicator);
            tabStudentLogin.setTextColor(0xFF999999);
            tabStudentLogin.setBackgroundColor(0x00000000);
        }
    }

    // ===== ROLE SELECT =====
    private void selectRole(String role) {
        selectedStaffRole = role;
        // CHANGED: Hindi → English
        tvSelectedRole.setText("✅ Role selected: " + role + " Staff");
        tvSelectedRole.setTextColor(0xFF4CAF50);

        cardRoleKitchen.setCardElevation("Kitchen".equals(role) ? 8f : 2f);
        cardRoleCounter.setCardElevation("Counter".equals(role) ? 8f : 2f);

        etStaffPassword.requestFocus();
    }

    // ===== STAFF PASSWORD VERIFY =====
    private void verifyStaffPassword() {
        if (selectedStaffRole.isEmpty()) {
            // CHANGED: Hindi → English
            Toast.makeText(this,
                    "Please select your role first!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String password = etStaffPassword.getText().toString().trim();
        if (password.isEmpty()) {
            // CHANGED: Hindi → English
            etStaffPassword.setError("Please enter password!");
            etStaffPassword.requestFocus();
            return;
        }

        if ("Kitchen".equals(selectedStaffRole)) {
            if (KITCHEN_PASSWORD.equals(password)) {
                // CHANGED: Hindi → English
                Toast.makeText(this,
                        "✅ Kitchen Staff verified!",
                        Toast.LENGTH_SHORT).show();
                goTo(KitchenActivity.class);
            } else {
                // CHANGED: Hindi → English
                etStaffPassword.setError("Incorrect password! Try again.");
                etStaffPassword.setText("");
                shakeView(etStaffPassword);
            }
        } else if ("Counter".equals(selectedStaffRole)) {
            if (COUNTER_PASSWORD.equals(password)) {
                // CHANGED: Hindi → English
                Toast.makeText(this,
                        "✅ Counter Manager verified!",
                        Toast.LENGTH_SHORT).show();
                goTo(CounterActivity.class);
            } else {
                // CHANGED: Hindi → English
                etStaffPassword.setError("Incorrect password! Try again.");
                etStaffPassword.setText("");
                shakeView(etStaffPassword);
            }
        }
    }

    // ===== GOOGLE SIGN-IN =====
    private void startGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed: " + e.getMessage());
                // CHANGED: Hindi → English
                Toast.makeText(this,
                        "Google Sign-In failed. Please try Guest login.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String name = (user != null && user.getDisplayName() != null)
                                ? user.getDisplayName() : "Student";

                        saveUserName(name);
                        // CHANGED: Hindi → English
                        Toast.makeText(this,
                                "Welcome, " + name + "! 🎉",
                                Toast.LENGTH_SHORT).show();
                        goTo(StudentActivity.class);
                    } else {
                        Log.e(TAG, "Firebase auth failed", task.getException());
                        // CHANGED: Hindi → English
                        Toast.makeText(this,
                                "Authentication failed! Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserName(String name) {
        getSharedPreferences("canteen_prefs", MODE_PRIVATE)
                .edit()
                .putString("student_name", name)
                .apply();
    }

    private void goTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void shakeView(View view) {
        view.animate()
                .translationX(16f).setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(-16f).setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(8f).setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(0f).setDuration(50)
                                        .start()).start()).start()).start();
    }
}