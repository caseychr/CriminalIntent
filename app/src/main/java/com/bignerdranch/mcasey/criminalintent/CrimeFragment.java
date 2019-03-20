package com.bignerdranch.mcasey.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnTextChanged;

public class CrimeFragment extends Fragment {

    private static final String FILE_PROVIDER = "com.bignerdranch.mcasey.criminalintent.fileprovider";

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_PHOTO = "DialogPhoto";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    View mView;
    Bitmap mBitmap;
    private Crime mCrime;
    private File mPhotoFile;
    private Callbacks mCallbacks;

    private int mPhotoWidth;
    private int mPhotoHeight;

    @BindView(R.id.crime_title) EditText mTitle;
    @BindView(R.id.crime_date) Button mDate;
    @BindView(R.id.crime_time) Button mTime;
    @BindView(R.id.crime_solved) CheckBox mSolved;
    @BindView(R.id.crime_suspect) Button mSuspectButton;
    @BindView(R.id.crime_phone) Button mPhoneButton;
    @BindView(R.id.crime_report) Button mReportButton;
    @BindView(R.id.crime_photo) ImageView mPhotoView;
    @BindView(R.id.crime_camera) ImageButton mPhotoButton;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
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
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.getInstance(getContext()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_crime:
                CrimeLab.getInstance(getActivity()).deleteCrime(mCrime);
                Intent intent = new Intent(getActivity(), CrimeListActivity.class);
                startActivity(intent);
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_crime, container, false);
        ButterKnife.bind(this, mView);
        mTitle.setText(mCrime.getTitle());
        updateDate();
        updateTime();
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });
        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });
        mSolved.setChecked(mCrime.isSolved());
        final Intent pickContact = new Intent(Intent.ACTION_DIAL, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

        //pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        mPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dial = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+mCrime.getPhone()));
                startActivity(dial);
            }
        });
        if(mCrime.getSuspect() != null)
            mSuspectButton.setText(mCrime.getSuspect());

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle(getString(R.string.send_report))
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport()).createChooserIntent();
                        /*new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                intent = Intent.createChooser(intent, getString(R.string.send_report));*/
                startActivity(intent);
            }
        });
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(getActivity(), FILE_PROVIDER, mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity().getPackageManager()
                        .queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity:cameraActivities)
                    getActivity().grantUriPermission(activity.activityInfo.packageName, uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DialogThumbnailFragment dialogThumbnailFragment = DialogThumbnailFragment.newInstance(mBitmap);
                dialogThumbnailFragment.show(manager, DIALOG_PHOTO);
            }
        });
        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mPhotoWidth = mPhotoView.getMeasuredWidth();
                    mPhotoHeight = mPhotoView.getMeasuredHeight();

                    updatePhotoView();
                }
            });
        }
        return mView;
    }

    @OnTextChanged (R.id.crime_title)
    public void updateText(CharSequence charSequence){
        mCrime.setTitle(charSequence.toString());
        updateCrime();
    }

    @OnCheckedChanged (R.id.crime_solved)
    public void isCrimeSolved(boolean b){
        mCrime.setSolved(b);
        updateCrime();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Date date;
        if(resultCode != Activity.RESULT_OK)
            return;
        if(requestCode == REQUEST_DATE){
            date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        }
        if(requestCode == REQUEST_TIME){
            date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateTime();
        } else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return values for
            String[] queryName =  new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            // Perform your query - the contactsUri is like a "where" clause here
            Cursor cursor = getActivity().getContentResolver().query(contactUri, queryName,
                    null, null, null);
            try{
                // Double-check that you actually got the results
                if(cursor.getCount() == 0)
                    return;
                // Pull out the first column of the first row of data -  that is your suspect's name
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                String phone = cursor.getString(1);
                mCrime.setSuspect(suspect);
                mCrime.setPhone(phone);
                updateCrime();
                mSuspectButton.setText(suspect);
            } finally {
                cursor.close();
            }
        } else if(requestCode == REQUEST_PHOTO){
            Uri uri = FileProvider.getUriForFile(getActivity(), FILE_PROVIDER, mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime(){
        CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDate() {
        mDate.setText(new SimpleDateFormat("EEEE, MMM dd, yyyy").format(mCrime.getDate()));
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved())
            solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null)
            suspect = getString(R.string.crime_report_no_suspect);
        else
            suspect = getString(R.string.crime_report_suspect, suspect);

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView(){
        if(mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        }
        else{
            mBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(mBitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }

    private void updateTime(){
        mTime.setText(new SimpleDateFormat("HH:mm:ss").format(mCrime.getDate()));
    }

}
