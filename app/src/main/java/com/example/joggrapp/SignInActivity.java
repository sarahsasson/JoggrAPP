package com.example.joggrapp; // <-- change to your package

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

// Firestore is optional. Comment these 4 lines if you don't want the upsert.
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // Set true to force account chooser on every launch (great for demos)
    private static final boolean FORCE_DEMO_SIGNIN_EVERY_LAUNCH = true;

    private GoogleSignInClient googleClient;
    private FirebaseAuth auth;

    private SignInButton btnGoogle;
    private ProgressBar progress;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account == null) {
                                showError("Google sign-in canceled");
                                return;
                            }
                            String idToken = account.getIdToken();
                            if (idToken == null) {
                                showError("Missing ID token");
                                Log.e(TAG, "idToken is null. Check default_web_client_id.");
                                return;
                            }
                            AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
                            auth.signInWithCredential(cred).addOnCompleteListener(this, t -> {
                                progress.setVisibility(View.GONE);
                                btnGoogle.setEnabled(true);
                                if (t.isSuccessful()) {
                                    FirebaseUser u = auth.getCurrentUser();
                                    // Optional: write/update user doc in Firestore
                                    upsertUser(u);
                                    goToProfile();
                                } else {
                                    Log.e(TAG, "Firebase sign-in FAILED", t.getException());
                                    showError("Authentication failed");
                                }
                            });
                        } catch (ApiException e) {
                            progress.setVisibility(View.GONE);
                            btnGoogle.setEnabled(true);
                            Log.e(TAG, "Google failed, code=" + e.getStatusCode(), e);
                            showError("Google failed (" + e.getStatusCode() + ")");
                        }
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnGoogle = findViewById(R.id.btn_google);
        progress = findViewById(R.id.progress);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from strings.xml
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // Demo mode: ensure picker shows every time the app opens
        if (FORCE_DEMO_SIGNIN_EVERY_LAUNCH) {
            auth.signOut();
            googleClient.signOut(); // clears cached Google account selection
            // googleClient.revokeAccess(); // uncomment if you also want to re-consent each time
        }

        btnGoogle.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            btnGoogle.setEnabled(false);
            signInLauncher.launch(googleClient.getSignInIntent());
        });

        // If you want to SKIP sign-in when already authenticated, re-enable this:
        // if (!FORCE_DEMO_SIGNIN_EVERY_LAUNCH && auth.getCurrentUser() != null) {
        //     goToProfile();
        // }
    }

    private void goToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    private void showError(String msg) {
        progress.setVisibility(View.GONE);
        btnGoogle.setEnabled(true);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ===== Firestore upsert (optional but handy) =====
    private void upsertUser(@Nullable FirebaseUser u) {
        if (u == null) return;
        try {
            Map<String, Object> user = new HashMap<>();
            user.put("uid", u.getUid());
            user.put("email", u.getEmail());
            user.put("displayName", u.getDisplayName());
            user.put("photoUrl", u.getPhotoUrl() != null ? u.getPhotoUrl().toString() : null);
            user.put("updatedAt", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(u.getUid())
                    .set(user, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e(TAG, "User upsert failed", e));
        } catch (Throwable t) {
            Log.w(TAG, "Firestore not available (ok if you didn't add dependency)", t);
        }
    }
}
