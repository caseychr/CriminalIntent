package com.bignerdranch.mcasey.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private static final int SERIOUS_CRIME = 1;
    public int adapterPosition;

    private RecyclerView mRecyclerView;
    private CrimeAdapter mAdapter;
    @BindView(R.id.empty_list_add) TextView mEmptyRVAdd;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        ButterKnife.bind(this, view);
        mRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(savedInstanceState != null)
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        updateUI();
        return view;
    }

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible)
            subtitleItem.setTitle(R.string.hide_subtitle);
        else
            subtitleItem.setTitle(R.string.show_subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.getInstance(getActivity()).addCrime(crime);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

    public void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);
        if(!mSubtitleVisible)
            subtitle = null;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if(crimes.isEmpty())
            mEmptyRVAdd.setVisibility(View.VISIBLE);
        else
            mEmptyRVAdd.setVisibility(View.INVISIBLE);
        if(mAdapter == null){
            mAdapter = new CrimeAdapter(crimes);
            mRecyclerView.setAdapter(mAdapter);
        }
        else
            mAdapter.setCrimes(crimes);
            mAdapter.notifyItemChanged(adapterPosition);
            //mAdapter.notifyDataSetChanged();
        updateSubtitle();
    }

    @OnClick (R.id.empty_list_add)
    public void newCrimeClick(){
        Crime crime = new Crime();
        CrimeLab.getInstance(getActivity()).addCrime(crime);
        Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
        startActivity(intent);
    }

    public class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected Crime mCrime;
        protected TextView mTitle;
        protected TextView mDate;
        protected ImageView mSolvedImageView;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater.inflate(layoutId, parent, false));
            itemView.setOnClickListener(this);
            mTitle = itemView.findViewById(R.id.crime_title);
            mDate = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime){
            mCrime = crime;
            mTitle.setText(mCrime.getTitle());
            mDate.setText(new SimpleDateFormat("EEEE, MMM dd, yyyy").format(mCrime.getDate()));
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE:View.GONE);
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onCrimeSelected(mCrime);
            adapterPosition = getAdapterPosition();
        }
    }

    public class SeriousCrimeHolder extends CrimeHolder{

        private TextView mSerious;

        public SeriousCrimeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater, parent, layoutId);
            itemView.setOnClickListener(this);
            mSerious = itemView.findViewById(R.id.crime_serious);
        }

        @Override
        public void onClick(View view) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
            adapterPosition = getAdapterPosition();
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){mCrimes = crimes;}

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            if(i == SERIOUS_CRIME)
                return new SeriousCrimeHolder(layoutInflater, viewGroup, R.layout.list_item_crime_serious);
            else
                return new CrimeHolder(layoutInflater, viewGroup, R.layout.list_item_crime);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder crimeHolder, int i) {
            Crime crime = mCrimes.get(i);
            crimeHolder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public int getItemViewType(int position) {
            if(mCrimes.get(position).isSerious())
                return 1;
            else
                return 0;
        }
    }
}
