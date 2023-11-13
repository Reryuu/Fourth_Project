package com.example.nreaderapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.nreaderapp.Constants;
import com.example.nreaderapp.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {
    private ActivityPdfViewBinding binding;
    private String bookId;
    private static final String TAG = "PDF_VIEW_TAG";

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get pdf url");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pdfUrl = "" + snapshot.child("url").getValue();
                Log.d(TAG, "onDataChange: " + pdfUrl);
                loadBookFromUrl(pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Load book");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                binding.progressBar.setVisibility(View.GONE);
                binding.pdfView.fromBytes(bytes).swipeHorizontal(false).onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        int currentPage = (page + 1);
                        binding.toolbarSubTitleTv.setText(currentPage + "/" + pageCount);
                        Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);
                    }
                }).onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: " + t.getMessage());
                        Toast.makeText(PdfViewActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        Log.d(TAG, "onPageError: " + t.getMessage());
                        Toast.makeText(PdfViewActivity.this, "Error on page" + page + "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).load();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: Book id " + bookId);

        loadBookDetails();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


}