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
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
