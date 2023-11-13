package com.example.nreaderapp.activities.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.nreaderapp.activities.MainActivity;
import com.example.nreaderapp.activities.ProfileActivity;
import com.example.nreaderapp.activities.user.DashBoardUserActivity;
import com.example.nreaderapp.adapters.AdapterCategory;
import com.example.nreaderapp.databinding.ActivityDashboardAdminBinding;
import com.example.nreaderapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityDashboardAdminBinding binding;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else {
            String email = firebaseUser.getEmail();
            binding.subtitleTv.setText(email);
        }
    }

    private void loadCategories() {
        categoryArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelCategory modelCategory = dataSnapshot.getValue(ModelCategory.class);

                    categoryArrayList.add(modelCategory);
                }
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                binding.categoriesRv.setAdapter(adapterCategory);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterCategory.getFilter().filter(charSequence);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });

        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, ProfileActivity.class));
            }
        });
    }
}