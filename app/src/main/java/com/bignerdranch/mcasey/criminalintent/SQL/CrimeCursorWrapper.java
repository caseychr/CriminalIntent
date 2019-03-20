package com.bignerdranch.mcasey.criminalintent.SQL;

import static com.bignerdranch.mcasey.criminalintent.SQL.CrimeDbSchema.CrimeTable.*;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.bignerdranch.mcasey.criminalintent.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime(){
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        long date = getLong(getColumnIndex(Cols.DATE));
        int isSolved = getInt(getColumnIndex(Cols.SOLVED));
        String suspect = getString(getColumnIndex(Cols.SUSPECT));
        String phone = getString(getColumnIndex(Cols.PHONE));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);
        crime.setPhone(phone);
        return crime;
    }
}
