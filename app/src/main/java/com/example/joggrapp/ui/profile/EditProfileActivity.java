package com.example.joggrapp.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.joggrapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfile";
    private static final String PREFS = "user_prefs";
    private static final String KEY_USE_LOCATION = "use_location";

    private Switch locationSwitch;
    private TextView statusText;

    // Fused Location
    private FusedLocationProviderClient fusedClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean requestingUpdates = false;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Back arrow in ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        locationSwitch = findViewById(R.id.switch_location);
        statusText = findViewById(R.id.tv_location_status);

        // --- Fused location setup ---
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(10_000) // 10s
                .setMinUpdateIntervalMillis(5_000)            // 5s min
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || result.getLastLocation() == null) return;
                double lat = result.getLastLocation().getLatitude();
                double lon = result.getLastLocation().getLongitude();
                Log.d(TAG, "Location: " + lat + "," + lon);
                // Keep UI simple for now; we just log updates.
            }
        };

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> result) -> {
                    boolean granted =
                            Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                                    Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    getSharedPreferences(PREFS, MODE_PRIVATE)
                            .edit().putBoolean(KEY_USE_LOCATION, granted).apply();
                    if (!granted) {
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                    updateUiAndMaybeToggleUpdates();
                });

        // Switch behavior: save preference, then start/stop updates as needed
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit().putBoolean(KEY_USE_LOCATION, isChecked).apply();

            if (isChecked) {
                if (!hasLocationPermission()) {
                    permissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                } else {
                    startLocationUpdatesIfNeeded();
                }
            } else {
                stopLocationUpdatesIfRunning();
                openAppSettingsToManagePermission(); // optional prompt
            }
            updateUiAndMaybeToggleUpdates();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiAndMaybeToggleUpdates(); // resume updates if user wants & permission granted
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Simple version: only track while this screen is visible
        stopLocationUpdatesIfRunning();
    }

    // ----- Helpers -----

    private boolean userWantsLocation() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_USE_LOCATION, false);
    }

    private boolean hasLocationPermission() {
        boolean fine = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fine || coarse;
    }

    private void startLocationUpdatesIfNeeded() {
        if (requestingUpdates) return;
        if (!userWantsLocation() || !hasLocationPermission()) return;

        fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
        requestingUpdates = true;
        Log.d(TAG, "Started location updates");
    }

    private void stopLocationUpdatesIfRunning() {
        if (!requestingUpdates) return;
        fusedClient.removeLocationUpdates(locationCallback);
        requestingUpdates = false;
        Log.d(TAG, "Stopped location updates");
    }

    private void openAppSettingsToManagePermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateUiAndMaybeToggleUpdates() {
        boolean wants = userWantsLocation();
        boolean hasPerm = hasLocationPermission();
        boolean effectiveOn = wants && hasPerm;

        // Keep the switch reflecting the effective state
        if (locationSwitch.isChecked() != wants) {
            locationSwitch.setOnCheckedChangeListener(null);
            locationSwitch.setChecked(wants);
            locationSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit().putBoolean(KEY_USE_LOCATION, isChecked).apply();
                if (isChecked) {
                    if (!hasLocationPermission()) {
                        permissionLauncher.launch(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });
                    } else {
                        startLocationUpdatesIfNeeded();
                    }
                } else {
                    stopLocationUpdatesIfRunning();
                    openAppSettingsToManagePermission();
                }
                updateUiAndMaybeToggleUpdates();
            });
        }

        statusText.setText(
                effectiveOn ? "Location is ON"
                        : wants && !hasPerm ? "Location ON in app, but OS permission is missing"
                        : "Location is OFF"
        );

        // Start/stop based on effective state
        if (effectiveOn) startLocationUpdatesIfNeeded();
        else stopLocationUpdatesIfRunning();
    }

    // Back arrow
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
