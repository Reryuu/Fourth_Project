package com.example.nreaderapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nreaderapp.adapters.AdapterPdfUser;
import com.example.nreaderapp.databinding.FragmentBookUserBinding;
import com.example.nreaderapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookUserFragment extends Fragment {

    private String categoryId, category, uid;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;
    private FragmentBookUserBinding binding;
    private static final String TAG = "BOOK_USER_TAG";

    public BookUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment BookUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookUserFragment newInstance(String categoryId, String category, String uid) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()), container, false);
        Log.d(TAG, "onCreateView: Category: " + category);
        if (category.equals("All")){
            loadAllBook();
        }else if (category.equals("Most Viewed")){
            loadMostViewedDowloadedBooks("viewsCount");
        }else if (category.equals("Most Dowloaded")){
            loadMostViewedDowloadedBooks("dowloadsCount");
        }else {
            loadCategorizedBooks();
        }

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterPdfUser.getFilter().filter(charSequence);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return binding.getRoot();
    }

    private void loadCategorizedBooks() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild("categoryId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelPdf model = dataSnapshot.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDowloadedBooks(String orderBy) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild(orderBy).limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelPdf model = dataSnapshot.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllBook() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelPdf model = dataSnapshot.getValue(ModelPdf.class);
                    pdfArrayList.add(model);
                }
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}