package com.example.joggrapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open ProfileActivity automatically
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);

        // Close MainActivity so it doesn't stay in the back
        finish();
    }
}
