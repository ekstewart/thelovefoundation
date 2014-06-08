package com.avadio.android.ilove.app;

import android.app.Application;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import database.MyContentProvider;

/**
 * Created by ljxi_828 on 6/7/14.
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        if( !checkDataBase() ) {
            // Run background task to load data into SQLite Database
            LoadQuotesTask loadQuotesTask = new LoadQuotesTask();
            loadQuotesTask.execute();
        }

    }

    /**
     * Check if the database exist
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        File dbFile = getDatabasePath("quotes.db");
        return dbFile.exists();
    }

    /* Use asyn task to load entries to the sqlite database */
    private class LoadQuotesTask extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {

            try {
                loadQuotes();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        public void onPostExecute(Void param) {
            super.onPostExecute(param);

            // Update UI
            //getSupportLoaderManager().initLoader(0, null, HomeActivity.this);
        }
    }


    /* Load quots from raw text file */
    private void loadQuotes() throws IOException {
        final Resources resources = getResources();
        InputStream inputStream = resources.openRawResource(R.raw.quotes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                //String[] strings = TextUtils.sp

                Uri uri;
                if (!line.equals("") && !line.startsWith("Quotes by")) {
                    uri = addQuote(line.trim());
                    if (uri.getLastPathSegment().equals("")) {
                        Log.e(TAG, "unable to add quote: " + line.trim());
                    }
                }
            }
        } finally {
            reader.close();
        }

    }

    /* Insert quote to SQLite database */
    public Uri addQuote(String quote) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("quote", quote);
        contentValues.put("author", "Harold B. Becker");

        return getContentResolver().insert(MyContentProvider.CONTENT_URI, contentValues);
    }
}
