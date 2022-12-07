package com.jopek.pai_fillo_nai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        TextView txtvOffline = findViewById(R.id.txtvOffline);
        txtvOffline.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, OfflineGameActivity.class);
            startActivity(intent);
        });
        TextView txtvOnline = findViewById(R.id.txtvOnline);
        txtvOnline.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, OnlineGameActivity.class);
            startActivity(intent);
        });
    }
}