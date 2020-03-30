package com.google.firebase.samples.apps.mlkit.java;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {


    private static String TAG = "Database"; // Tag just for the LogCat window
    private static String DB_NAME ="product_database.db"; // Database name
    private static int DB_VERSION = 1; // Database version
    private final File DB_FILE;
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        DB_FILE = context.getDatabasePath(DB_NAME);
        this.mContext = context;
    }

    public void createDataBase() throws IOException {
        // If the database does not exist, copy it from the assets.

        //Setting mDatabaseExist to false means that every time the app is run, the database is copied from assets
        boolean mDataBaseExist = false;//checkDataBase();
        if(!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                // Copy the database from assets
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                Log.e(TAG, "Error createDatabase");
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    // Check that the database file exists in databases folder
    private boolean checkDataBase() {
        return DB_FILE.exists();
    }

    // Copy the database from assets
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        OutputStream mOutput = new FileOutputStream(DB_FILE.getAbsolutePath());
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    // Open the database, so we can query it
    public boolean openDataBase() throws SQLException {
        // Log.v("DB_PATH", DB_FILE.getAbsolutePath());
        mDataBase = SQLiteDatabase.openDatabase(DB_FILE.getAbsolutePath(), null, SQLiteDatabase.CREATE_IF_NECESSARY);
        // mDataBase = SQLiteDatabase.openDatabase(DB_FILE, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if(mDataBase != null) {
            mDataBase.close();
        }
        super.close();
    }


//    public ArrayList<String> getMatches(String searchString) {
//        ArrayList<String> array_list = new ArrayList<String>();
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        String[] selectionArgs = { searchString };
//
//        Cursor res =  db.rawQuery( "SELECT * FROM fts_table WHERE name='%"+searchString+"%'", null);
//        res.moveToFirst();
//
//        while(res.isAfterLast() == false){
//            array_list.add(res.getString(0));
//            res.moveToNext();
//        }
//
//        res.close();
//        //Log.d(TAG,"size " +String.valueOf(array_list.size()));
//        db.close();
//        return array_list;
//    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
