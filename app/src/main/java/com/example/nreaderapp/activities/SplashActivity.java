package com.example.nreaderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.nreaderapp.R;
import com.example.nreaderapp.activities.admin.DashboardAdminActivity;
import com.example.nreaderapp.activities.user.DashBoardUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userType = "" + snapshot.child("userType").getValue();
                    if (userType.equals("user")){
                        startActivity(new Intent(SplashActivity.this, DashBoardUserActivity.class));
                        finish();
                    }else if (userType.equals("admin")){
                        startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 1750);
    }
}