package com.example.joggrapp.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
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

    private Switch locationSwitch;
    private TextView statusText;

    // Location
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Enable default back arrow if your theme shows an ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        locationSwitch = findViewById(R.id.switch_location);
        statusText = findViewById(R.id.tv_location_status);

        // --- Fused Location setup ---
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Request ~10s updates (balanced power)
        locationRequest = new LocationRequest.Builder(
                /* intervalMillis = */ 10_000L)
                .setMinUpdateIntervalMillis(5_000L)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || result.getLastLocation() == null) return;
                double lat = result.getLastLocation().getLatitude();
                double lon = result.getLastLocation().getLongitude();
                statusText.setText("Location: " + String.format("%.5f, %.5f", lat, lon));
            }
        };

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> res) -> {
                    boolean granted =
                            Boolean.TRUE.equals(res.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                                    Boolean.TRUE.equals(res.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                    // Persist user preference (whether they want location ON)
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit().putBoolean("use_location", granted).apply();

                    if (!granted) {
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                    updateUi(); // will start/stop updates as needed
                });

        // Switch listener
        locationSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                // User wants it ON → ensure permission then start updates
                if (!hasLocationPermission()) {
                    permissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                } else {
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit().putBoolean("use_location", true).apply();
                    startLocationUpdates();
                    updateUi();
                }
            } else {
                // User toggled OFF → stop updates (and keep permission as-is)
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit().putBoolean("use_location", false).apply();
                stopLocationUpdates();
                statusText.setText("Location is OFF");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If user wants location and permission is granted, (re)start updates
        boolean wants = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("use_location", false);
        if (wants && hasLocationPermission()) {
            startLocationUpdates();
        }
        updateUi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Be polite with battery: stop when screen not visible
        stopLocationUpdates();
    }

    private boolean hasLocationPermission() {
        boolean fine = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fine || coarse;
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) return;
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        // Also try a quick “last location” to show something immediately
        fusedClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                statusText.setText("Location: " +
                        String.format("%.5f, %.5f", loc.getLatitude(), loc.getLongitude()));
            }
        });
    }

    private void stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback);
    }

    private void openAppSettingsToManagePermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateUi() {
        boolean wantsLocation = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("use_location", false);
        boolean hasPerm = hasLocationPermission();
        boolean effectiveOn = wantsLocation && hasPerm;

        // Keep switch in sync without retriggering listener loops
        if (locationSwitch.isChecked() != effectiveOn) {
            locationSwitch.setOnCheckedChangeListener(null);
            locationSwitch.setChecked(effectiveOn);
            locationSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    if (!hasLocationPermission()) {
                        permissionLauncher.launch(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });
                    } else {
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit().putBoolean("use_location", true).apply();
                        startLocationUpdates();
                        updateUi();
                    }
                } else {
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit().putBoolean("use_location", false).apply();
                    stopLocationUpdates();
                    statusText.setText("Location is OFF");
                }
            });
        }

        if (effectiveOn) {
            // If updates are running, text will show coordinates from callback
            if (!hasPerm) statusText.setText("Location ON (no permission?)");
        } else if (wantsLocation && !hasPerm) {
            statusText.setText("Location ON in app, but OS permission missing");
        } else {
            statusText.setText("Location is OFF");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
