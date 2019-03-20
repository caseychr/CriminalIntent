package com.bignerdranch.mcasey.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment {

    public static final String EXTRA_TIME = "com.bignerdranch.mcasey.criminalintent.time";

    private static final String ARG_TIME = "time";

    private TimePicker mTimePicker;
    Calendar mCalendar;
    Date mDate;

    public static TimePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_TIME);
        mCalendar = Calendar.getInstance();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
        mTimePicker = view.findViewById(R.id.dialog_time_picker);
        return new AlertDialog.Builder(getActivity()).setView(view).setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCalendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getHour());
                        mCalendar.set(Calendar.MINUTE, mTimePicker.getMinute());
                        mCalendar.set(Calendar.SECOND, 00);
                        mDate = mCalendar.getTime();
                        sendResult(Activity.RESULT_OK, mDate);
                    }
                }).create();
    }

    private void sendResult(int resultCode, Date date){
        if(getTargetFragment() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, date);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
