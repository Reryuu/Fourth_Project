package com.example.nreaderapp.activities.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.nreaderapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private static final String TAG = "ADD_PDF_TAG";
    private static final int PDF_PICK_CODE = 1000;

    private String title = "",description = "", category = "";
    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri pdfUri = null;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private String selectedCategoryId, selectedCategoryTitle;

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: pdf categories loading");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String categoryId = "" + dataSnapshot.child("id").getValue();
                    String categoryTitle = "" + dataSnapshot.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: category pick dialog showing");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick category").setItems(categoriesArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                category = categoriesArray[i];

                selectedCategoryTitle = categoryTitleArrayList.get(i);
                selectedCategoryId = categoryIdArrayList.get(i);
                binding.categoryTv.setText(selectedCategoryTitle);
                Log.d(TAG, "onClick: Category deleted" + selectedCategoryId + "" + selectedCategoryTitle);
            }
        }).show();
    }
    private void validateData() {
        Log.d(TAG, "validateData: validating data");
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        category = binding.categoryTv.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "Please choose category", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri == null){
            Toast.makeText(this, "Please choose Pdf", Toast.LENGTH_SHORT).show();
        }else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage");
        progressDialog.setMessage("Pdf uploading");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        String filePathAndName = "Books/" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: Pdf uploaded");
                Log.d(TAG, "onSuccess: getting pdf url");
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = "" + uriTask.getResult();
                uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: Pdf upload failed due to " + e.getMessage());
                Toast.makeText(PdfAddActivity.this, "Pdf upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfToStorage: uploading to database");
        progressDialog.setMessage("Pdf data uploading");
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("url", "" + uploadedPdfUrl);
        hashMap.put("viewsCount", 0);
        hashMap.put("dowloadsCount", 0);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child("" + timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Log.d(TAG, "onSuccess: Uploaded success");
                Toast.makeText(PdfAddActivity.this, "Uploaded success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: Failled upload due to " + e.getMessage());
                Toast.makeText(PdfAddActivity.this, "Failled upload due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF picked");
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult: URI" + pdfUri);

            }
        }else {
            Log.d(TAG, "onActivityResult: pdf picking cancelled");
            Toast.makeText(this, "pdf picking cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}