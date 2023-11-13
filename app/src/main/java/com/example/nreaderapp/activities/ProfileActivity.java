package com.example.nreaderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.nreaderapp.MyApplication;
import com.example.nreaderapp.R;
import com.example.nreaderapp.adapters.AdapterPdfFavorite;
import com.example.nreaderapp.databinding.ActivityProfileBinding;
import com.example.nreaderapp.models.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;
    private static final String TAG = "PROFILE_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadFavoriteBooks();

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading" + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = "" + snapshot.child("email").getValue();
                String name = "" + snapshot.child("name").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();
                String uid = "" + snapshot.child("uid").getValue();
                String userType = "" + snapshot.child("userType").getValue();

                String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                binding.emailTv.setText(email);
                binding.nameTv.setText(name);
                binding.memberDateTv.setText(formattedDate);
                binding.accountTypeTv.setText(userType);

                Glide.with(ProfileActivity.this).load(profileImage).placeholder(R.drawable.ic_person_g).into(binding.profileIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFavoriteBooks() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String bookId = "" + dataSnapshot.child("bookId").getValue();

                    ModelPdf modelPdf = new ModelPdf();
                    modelPdf.setId(bookId);
                    pdfArrayList.add(modelPdf);
                }

                binding.favoriteBookCountTv.setText("" + pdfArrayList.size());
                adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfFavorite);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}