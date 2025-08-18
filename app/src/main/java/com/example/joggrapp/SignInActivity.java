package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private SignInButton btnGoogle;
    private ProgressBar progress;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        signInWithFirebase(account.getIdToken());
                    } else {
                        showError("Google sign-in canceled");
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "Google sign-in failed", e);
                    showError("Google sign-in failed");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnGoogle = findViewById(R.id.btn_google);
        progress  = findViewById(R.id.progress);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();

        btnGoogle.setOnClickListener(v -> {
            progress.setVisibility(ProgressBar.VISIBLE);
            btnGoogle.setEnabled(false);
            signInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current = firebaseAuth.getCurrentUser();
        if (current != null) {
            goToProfile();
        }
    }

    private void signInWithFirebase(String idToken) {
        if (idToken == null) {
            showError("Missing ID token");
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progress.setVisibility(ProgressBar.GONE);
                    btnGoogle.setEnabled(true);
                    if (task.isSuccessful()) {
                        goToProfile();
                    } else {
                        Log.e(TAG, "Firebase sign-in failed", task.getException());
                        showError("Authentication failed");
                    }
                });
    }

    private void goToProfile() {
        Intent i = new Intent(SignInActivity.this, ProfileActivity.class);
        startActivity(i);
        finish();
    }

    private void showError(String msg) {
        progress.setVisibility(ProgressBar.GONE);
        btnGoogle.setEnabled(true);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
