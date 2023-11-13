package com.example.nreaderapp.activities.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.nreaderapp.BookUserFragment;
import com.example.nreaderapp.activities.MainActivity;
import com.example.nreaderapp.activities.ProfileActivity;
import com.example.nreaderapp.databinding.ActivityDashBoardUserBinding;
import com.example.nreaderapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashBoardUserActivity extends AppCompatActivity {

    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private ActivityDashBoardUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private  void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        categoryArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                ModelCategory modelAll = new ModelCategory("01", "All", "", "1");
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", "1");
                ModelCategory modelMostDowloaded = new ModelCategory("03", "Most Dowloaded", "", "1");

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDowloaded);

                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelAll.getId() , "" + modelAll.getCategory(), "" + modelAll.getUid()), modelAll.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelMostViewed.getId() , "" + modelMostViewed.getCategory(), "" + modelMostViewed.getUid()), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelMostDowloaded.getId() , "" + modelMostDowloaded.getCategory(), "" + modelMostDowloaded.getUid()), modelMostDowloaded.getCategory());
                viewPagerAdapter.notifyDataSetChanged();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelCategory modelCategory = dataSnapshot.getValue(ModelCategory.class);
                    categoryArrayList.add(modelCategory);
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance("" + modelCategory.getId(), "" + modelCategory.getCategory(), "" + modelCategory.getUid()), modelCategory.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BookUserFragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            binding.subtitleTv.setText("Not logged in");
            binding.profileBtn.setVisibility(View.GONE);
            binding.logoutBtn.setVisibility(View.GONE);
        }else {
            String email = firebaseUser.getEmail();
            binding.subtitleTv.setText(email);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashBoardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashBoardUserActivity.this, ProfileActivity.class));
            }
        });
    }
}