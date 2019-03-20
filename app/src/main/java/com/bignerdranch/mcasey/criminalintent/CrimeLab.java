package com.bignerdranch.mcasey.criminalintent;

import static com.bignerdranch.mcasey.criminalintent.SQL.CrimeDbSchema.CrimeTable.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.mcasey.criminalintent.SQL.CrimeBaseHelper;
import com.bignerdranch.mcasey.criminalintent.SQL.CrimeCursorWrapper;
import com.bignerdranch.mcasey.criminalintent.SQL.CrimeDbSchema;
import com.bignerdranch.mcasey.criminalintent.SQL.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Map<UUID, Crime> mCrimeMap;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab getInstance(Context context) {
        if(sCrimeLab == null)
            sCrimeLab = new CrimeLab(context);
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
        /*for(int i=0;i<100;i++){
            Crime crime = new Crime();
            crime.setTitle("Crime #"+i);
            crime.setSolved(i%2==0);
            crime.setSerious(i%2==0);
        }*/
    }

    public List<Crime> getCrimes(){
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return crimes;
    }

    public Crime getCrime(UUID id){
        CrimeCursorWrapper cursor = queryCrimes(Cols.UUID+" = ?",
                new String[] {id.toString()});
        try{
            if(cursor.getCount()==0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();
        values.put(Cols.UUID, crime.getId().toString());
        values.put(Cols.TITLE, crime.getTitle());
        values.put(Cols.DATE, crime.getDate().getTime());
        values.put(Cols.SOLVED, crime.isSolved() ? 1:0);
        values.put(Cols.SUSPECT, crime.getSuspect());
        return  values;
    }

    public void addCrime(Crime crime){
        ContentValues values = getContentValues(crime);
        mDatabase.insert(NAME, null, values);
    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        mDatabase.update(NAME, values, Cols.UUID+" = ?", new String[]{uuidString});
    }

    public File getPhotoFile(Crime crime){
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                NAME,
                null, //columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null //orderBy
        );
        return new CrimeCursorWrapper(cursor);
    }

    public void deleteCrime(Crime crime){
        //mCrimes.remove(crime);
        mDatabase.delete(NAME,  Cols.UUID+" = ?", new String[]{crime.getId().toString()});
    }

    public Crime getCrimeQuicker(UUID id){
        return mCrimeMap.get(id);
    }

}
