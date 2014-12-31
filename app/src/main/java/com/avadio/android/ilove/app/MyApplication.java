package com.avadio.android.ilove.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import database.QuotesDatabaseHelper;

/**
 * Created by ljxi_828 on 6/7/14.
 */
public class MyApplication extends Application {
    public static final String MY_PREFS_NAME = "INSPIRE_LOVE_PREFS";

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = null;

        // Used to trigger database onUpgrade method so that we can set a sharedPref flag to load
        try {
            QuotesDatabaseHelper quotesDatabaseHelper = new QuotesDatabaseHelper(this);
            db = quotesDatabaseHelper.getReadableDatabase();
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
