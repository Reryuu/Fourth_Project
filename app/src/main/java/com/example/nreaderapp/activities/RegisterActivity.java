package com.example.nreaderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.nreaderapp.activities.user.DashBoardUserActivity;
import com.example.nreaderapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ActivityRegisterBinding binding;
    private String name = "", email = "", password = "";
    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String Cpassword = binding.cpasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(Cpassword)){
            Toast.makeText(this, "Confirm password", Toast.LENGTH_LONG).show();
        }else if(!password.equals(Cpassword)){
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_LONG).show();
        }else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
//        progressDialog.setMessage("Creating account");
//        progressDialog.show();

        //create user in firebase
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                updateUserInfo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user information");
        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(RegisterActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);




        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.this.runOnUiThread(()->{
                    validateData();
                });

            }
        });
    }
}