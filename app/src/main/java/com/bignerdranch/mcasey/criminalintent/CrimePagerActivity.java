package com.bignerdranch.mcasey.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private static final String EXTRA_CRIME_ID = "com.bignerdranch.mcasey.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    private Button mFirstBtn;
    private Button mLastBtn;

    public static Intent newIntent(Context context, UUID crimeId){
        Intent intent = new Intent(context, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        mViewPager = findViewById(R.id.crime_view_pager);
        mCrimes = CrimeLab.getInstance(this).getCrimes();
        mFirstBtn = findViewById(R.id.view_pager_first);
        mLastBtn = findViewById(R.id.view_pager_last);
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                Crime crime = mCrimes.get(i);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        for(int i=0;i<mCrimes.size();i++){
            if(mCrimes.get(i).getId().equals(crimeId)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }

        mFirstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(0);
                mFirstBtn.setVisibility(View.GONE);
            }
        });
        mLastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mCrimes.size()-1);
                mLastBtn.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mViewPager.getCurrentItem()==0){
            mFirstBtn.setVisibility(View.GONE);
        }
        else if(mViewPager.getCurrentItem()==mCrimes.size()-1){
            mLastBtn.setVisibility(View.GONE);
        }
        else{
            mFirstBtn.setVisibility(View.VISIBLE);
            mLastBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {

    }
}
