package com.example.nreaderapp;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.nreaderapp.adapters.AdapterPdfAdmin;
import com.example.nreaderapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    private static final long MAX_BYTES_PDF = 50000000;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        Log.d(TAG, "deleteBook: deleting");
        progressDialog.setMessage("Deleting " + bookTitle);
        progressDialog.show();

        Log.d(TAG, "deleteBook: Delete from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Deleted");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                databaseReference.child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from database");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Delete failed due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Delete failed due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG, "onSuccess: " + pdfTitle + "" + bytes);
                double kb = bytes/1024;
                double mb = kb/1024;

                if (mb >= 1){
                    sizeTv.setText(String.format("%.2f", mb) + " Mb");
                }else if (kb >= 1){
                    sizeTv.setText(String.format("%.2f", kb) + " Kb");
                }else {
                    sizeTv.setText(String.format("%.2f", bytes) + " b");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView , ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess: " + pdfTitle + "got the file successfully");

                pdfView.fromBytes(bytes).pages(0).spacing(0).swipeHorizontal(false).enableSwipe(false).onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onError: " + t.getMessage());
                    }
                }).onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onPageError: " + t.getMessage());
                    }
                }).onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "loadComplete: Pdf loaded");

                        if (pagesTv != null){
                            pagesTv.setText("" + nbPages);
                        }
                    }
                }).load();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onFailure: file getting failled due to " + e.getMessage());
            }
        });
    }

    public static void loadCategory(String categoryId, TextView categoryTv) {
//        ModelPdf modelPdf;
//        categoryId = modelPdf.getCategoryId();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String category = "" + snapshot.child("category").getValue();
                categoryTv.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void incrementBookViewCount(String bookId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String viewsCount = "" + snapshot.child("viewsCount").getValue();
                if (viewsCount.equals("") || viewsCount.equals("null")){
                    viewsCount = "0";
                }
                long newViewsCount = Long.parseLong(viewsCount) + 1;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("viewsCount", newViewsCount);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                databaseReference.child(bookId).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void dowloadBook(Context context, String bookId, String bookTitle, String bookUrl){
        String TAG_DOWLOAD = "DOWLOAD_TAG";
        Log.d(TAG_DOWLOAD, "dowloadBook: dowloading");
        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWLOAD, "dowloadBook: Name: " + nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Dowloading " + nameWithExtension);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage
                .getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG_DOWLOAD, "onSuccess: Book dowloaded");
                saveDowloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG_DOWLOAD, "onFailure: Dowload failed due to " + e.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "Dowload failed due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void saveDowloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        String TAG_DOWLOAD = "DOWLOAD_TAG";
        Log.d(TAG_DOWLOAD, "onSuccess: Saving");
        try {
            File dowloadedFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            dowloadedFolder.mkdir();

            String filePath = dowloadedFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Save to dowloaded folde", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWLOAD, "saveDowloadedBook: Save to dowloaded folder");
            progressDialog.dismiss();
            incrementBookDowloadCount(bookId);
        }catch (Exception e){
            Log.d(TAG_DOWLOAD, "saveDowloadedBook: Failed due to " + e.getMessage());
            Toast.makeText(context, "Failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDowloadCount(String bookId) {
        String TAG_DOWLOAD = "DOWLOAD_TAG";
        Log.d(TAG_DOWLOAD, "incrementBookDowloadCount: Incrementing book dowload count");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dowloadsCount = "" + snapshot.child("dowloadsCount");
                Log.d(TAG_DOWLOAD, "onDataChange: Dowload count" + dowloadsCount);
                if (dowloadsCount.equals("") || dowloadsCount.equals("null")){
                    dowloadsCount = "0";
                }
                 long newDowloadsCount = Long.parseLong(dowloadsCount) + 1;
                 Log.d(TAG_DOWLOAD, "onDataChange: New dowload count: " + newDowloadsCount);
                 HashMap<String,Object> hashMap = new HashMap<>();
                 hashMap.put("dowloadsCount", newDowloadsCount);
                 DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                 reference.child(bookId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void unused) {
                         Log.d(TAG_DOWLOAD, "onSuccess: Counts update");
                     }
                 }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Log.d(TAG_DOWLOAD, "onFailure: Failed to update count due to " + e.getMessage());
                     }
                 });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void addToFavorite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
        }else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" + bookId);
            hashMap.put("timestamp", "" + timestamp);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Favorite added", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Add failed due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void removeFromFavorite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
        }else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Favorite removed", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Removed failed due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
