package com.bignerdranch.mcasey.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.util.Date;
import java.util.GregorianCalendar;

public class DialogThumbnailFragment extends DialogFragment {

    private static final String ARG_BITMAP = "bitmap";

    private ImageView mImageView;
    static Bitmap mBitmap;

    public static DialogThumbnailFragment newInstance(Bitmap bitmap){
        Bundle args = new Bundle();
        //args.putParcelable(ARG_BITMAP, bitmap);
        mBitmap = bitmap;
        DialogThumbnailFragment fragment = new DialogThumbnailFragment();
        //fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_thumbnail, null);
        mImageView = view.findViewById(R.id.dialog_thumbnail_image);
        mImageView.setImageBitmap(mBitmap);
        return new AlertDialog.Builder(getActivity()).setView(view).setTitle("IMAGE")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /*int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                        Date date = new GregorianCalendar(year, month, day).getTime();
                        sendResult(Activity.RESULT_OK, date);*/
                    }
                }).create();
    }
}
