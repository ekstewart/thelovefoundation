package com.tlf.android.inspirelove.app;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.tlf.android.inspirelove.database.MyContentProvider;
import com.tlf.android.inspirelove.datamodel.QUOTESTABLE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuotesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = QuotesFragment.class.getSimpleName();

    private Cursor mCursor;
    private int mIndex = -1;
    private TextView mQuoteText;
    private ShareActionProvider mShareActionProvider;

    // Required default constructor for fragment
    public QuotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mQuoteText = (TextView) root.findViewById(R.id.quote);

        mQuoteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor != null && mCursor.getCount() > 0) {
                    // Move cursor to a random position
                    mIndex = randInt(0, mCursor.getCount() - 1);
                    mCursor.moveToPosition(mIndex);
                    int columnIndex = mCursor.getColumnIndex(QUOTESTABLE.COL_QUOTE);

                    String quote = mCursor.getString(columnIndex);
                    mQuoteText.setText(quote);
                    mQuoteText.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));

                    // Update share provider content
                    mShareActionProvider.setShareIntent(getDefaultIntent());
                }
            }
        });

        // Get from the retain fragment
        if (mCursor != null && mIndex != -1) {
            mCursor.moveToPosition(mIndex);
            int columnIndex = mCursor.getColumnIndex(QUOTESTABLE.COL_QUOTE);

            String quote = mCursor.getString(columnIndex);
            mQuoteText.setText(quote);
            mQuoteText.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));

            // Update share provider content
            mShareActionProvider.setShareIntent(getDefaultIntent());
        }
        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get the cursor object from retain fragment
        final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(this
                .getFragmentManager());

        Cursor cursor = (Cursor) mRetainFragment.getObject();

        if (cursor != null) {
            mCursor = cursor;
            mIndex = mRetainFragment.getIndex();
        } else {
            SharedPreferences prefs = getActivity().getSharedPreferences(MyApplication.MY_PREFS_NAME, getActivity().MODE_PRIVATE);
            final Boolean dataLoaded = prefs.getBoolean("quotesLoaded", false);
            if (dataLoaded != true) {
                // Run background task to load data into SQLite Database
                LoadQuotesTask loadQuotesTask = new LoadQuotesTask();
                loadQuotesTask.execute();
                // Set variable to true
            } else {
                getActivity().getSupportLoaderManager().initLoader(0, null, this);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save the current quote index
//            SharedPreferences prefs = this.getActivity().getSharedPreferences(
//                    "com.tlf.android.inspirelove.app", Context.MODE_PRIVATE);
//
//            prefs.edit().putString("quote-text", mQuoteText.getText().toString());
//            prefs.edit().commit();

        // Save the data object to the retain fragment to handle orientation change
        final RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(this
                .getFragmentManager());

        mRetainFragment.setObject(mCursor);
        mRetainFragment.setIndex(mIndex);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {QUOTESTABLE.COL_ID, QUOTESTABLE.COL_QUOTE};
        return new CursorLoader(
                this.getActivity(),
                MyContentProvider.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    /**
     * It will be called by:  getContext().getContentResolver().notifyChange(uri, null);
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Number of quotes loaded = " + data.getCount());
        mCursor = data;

        // Read from cursor
        if (mCursor.getCount() > 0) {
            // Move cursor to a random position
            mIndex = randInt(0, mCursor.getCount() - 1);
            mCursor.moveToPosition(mIndex);

            int idIndex = mCursor.getColumnIndex(QUOTESTABLE.COL_QUOTE);
            String quote = mCursor.getString(idIndex);
            // TODO:  Get the cursor from saved fragment
            mQuoteText.setText(quote);
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(getDefaultIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete reference
    }

    /**
     * Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, mQuoteText == null ? "Temp" : mQuoteText.getText() + "\nBy: Harold B. Becker");
        return intent;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public int randInt(int min, int max) {
        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    /* Load quots from raw text file */
    private void loadQuotes() throws IOException {
        final Resources resources = getResources();
        InputStream inputStream = resources.openRawResource(R.raw.quotes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int count = 0;
        final int size = 880;
        ContentValues[] contentValues = new ContentValues[size];

        try {
            String line;
            while ((line = reader.readLine()) != null) {
//                Uri uri;
                if (!line.equals("") && !line.startsWith("Quotes by")) {
                    contentValues[count] = new ContentValues();
                    contentValues[count].put("quote", line.trim());
                    contentValues[count].put("author", "Harold B. Becker");
                    count++;
//                    uri = addQuote(line.trim());
//                    if (uri.getLastPathSegment().equals("")) {
//                        Log.e(TAG, "unable to add quote: " + line.trim());
//                    }
                }
            }

            final int total = getActivity().getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI, contentValues);
            if (total != size) {
                Log.e(TAG, "Unexpected total: " + total);
            }
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(MyApplication.MY_PREFS_NAME, getActivity().MODE_PRIVATE).edit();
            editor.putBoolean("quotesLoaded", true);
            editor.commit();
        } finally {
            reader.close();
        }
    }

    /* Insert quote to SQLite database */
    public Uri addQuote(String quote) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("quote", quote);
        contentValues.put("author", "Harold B. Becker");

        return getActivity().getContentResolver().insert(MyContentProvider.CONTENT_URI, contentValues);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.home, menu);

        // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            startActivity(new Intent(this.getActivity(), AboutUsActivity.class));
        }
        if (id == R.id.action_donate) {
            startActivity(new Intent(this.getActivity(), DonateWelcomeActivity.class));
        }
        return super.onOptionsItemSelected(item);
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
            getActivity().getSupportLoaderManager().initLoader(0, null, QuotesFragment.this);
        }
    }
}
