package com.example.nreaderapp.activities.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.nreaderapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private static final String TAG = "BOOK_EDIT_TAG";
    private ActivityPdfEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private String selectedCategoryId = "", selectedCategoryTitle = "", title = "", description = "";
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedCategoryId = categoryIdArrayList.get(i);
                selectedCategoryTitle = categoryTitleArrayList.get(i);

                binding.categoryTv.setText(selectedCategoryTitle);

            }
        }).show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Categories loading");
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String id = "" + dataSnapshot.child("id").getValue();
                    String category = "" + dataSnapshot.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: Id: " + id);
                    Log.d(TAG, "onDataChange: Category " + category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Book info loading");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                String description = "" + snapshot.child("description").getValue();
                String title = "" + snapshot.child("title").getValue();

                binding.titleEt.setText(title);
                binding.descriptionEt.setText(description);
                Log.d(TAG, "onDataChange: Book category info loading");
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");
                databaseReference.child(selectedCategoryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = "" + snapshot.child("category").getValue();
                        binding.categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Please choose category", Toast.LENGTH_SHORT).show();
        }else {
            updatePfd();
        }
    }

    private void updatePfd() {
        Log.d(TAG, "updatePfd: Update starting");
        progressDialog.setMessage("Updating book data");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);

        DatabaseReference reference =FirebaseDatabase
                .getInstance().getReference("Books");
        reference.child(bookId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Book updated");
                progressDialog.dismiss();
                Toast.makeText(PdfEditActivity.this,
                        "Book data updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Failed due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(PdfEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}