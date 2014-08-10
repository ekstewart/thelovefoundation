package com.avadio.android.ilove.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import database.MyContentProvider;
import datamodel.QUOTESTABLE;


public class HomeActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private Cursor mCursor;
    private TextView mQuoteText;
    private ShareActionProvider mShareActionProvider;

    Animation mAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        getSupportLoaderManager().initLoader(0, null, HomeActivity.this);

        //TODO:  Don't change quote on back
        // Run background task to load data into SQLite Database
//        LoadQuotesTask loadQuotesTask = new LoadQuotesTask();
//        loadQuotesTask.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());

        return super.onCreateOptionsMenu(menu);
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
        intent.putExtra(Intent.EXTRA_TEXT, mQuoteText.getText() == null ? "" : mQuoteText.getText());
//        intent.putExtra(Intent.EXTRA_TEXT, "Test shareIntent");
        //TODO:  format shared quote

        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutUsActivity.class));
        }
        if (id == R.id.action_donate) {
            startActivity(new Intent(this, DonateWelcomeActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {QUOTESTABLE.COL_ID, QUOTESTABLE.COL_QUOTE};
        CursorLoader loader = new CursorLoader(
                this,
                MyContentProvider.CONTENT_URI,
                projection,
                null,
                null,
                null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;

        // Read from cursor
        if (mCursor != null && mCursor.getCount() > 0) {
//            mCursor.moveToFirst();
            // Move to random row
            int row = randInt(0, mCursor.getCount());
            mCursor.moveToPosition(row);

            int idIndex = mCursor.getColumnIndex(QUOTESTABLE.COL_QUOTE);

            String quote = mCursor.getString(idIndex);

            mQuoteText = (TextView) findViewById(R.id.quote);
            mQuoteText.setText(quote);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete reference
    }

    public void OnTextClick(View view) {
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToNext();
            int idIndex = mCursor.getColumnIndex(QUOTESTABLE.COL_QUOTE);

            String quote = mCursor.getString(idIndex);

//            mAnimation = AnimationUtils.loadAnimation(this, R.anim.translate);
//            mQuoteText.startAnimation(mAnimation);

            mQuoteText.setText(quote);

            mQuoteText.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));

            // Update share provider content
            mShareActionProvider.setShareIntent(getDefaultIntent());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }
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
            getSupportLoaderManager().initLoader(0, null, HomeActivity.this);
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
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
