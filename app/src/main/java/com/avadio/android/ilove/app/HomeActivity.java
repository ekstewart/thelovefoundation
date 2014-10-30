package com.avadio.android.ilove.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
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

import java.util.Random;

import database.MyContentProvider;
import datamodel.QUOTESTABLE;


public class HomeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String TAG = PlaceholderFragment.class.getSimpleName();

        private Cursor mCursor;
        private int mIndex = -1;
        private TextView mQuoteText;
        private ShareActionProvider mShareActionProvider;

        public PlaceholderFragment() {
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
                getSupportLoaderManager().initLoader(0, null, this);
            }
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

        @Override
        public void onPause() {
            super.onPause();
            // Save the current quote index
//            SharedPreferences prefs = this.getActivity().getSharedPreferences(
//                    "com.avadio.android.ilove.app", Context.MODE_PRIVATE);
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

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.d(TAG, "Number of quotes loaded = " + data.getCount());
            mCursor = data;

            // Read from cursor
            if (mCursor != null && mCursor.getCount() > 0) {
//            mCursor.moveToFirst();
                // Move cursor to a random position
                mIndex = randInt(0, mCursor.getCount() -1);
                mCursor.moveToPosition(mIndex);

                int idIndex = mCursor.getColumnIndex(QUOTESTABLE.COL_QUOTE);
                String quote = mCursor.getString(idIndex);
//                mQuoteText = (TextView) findViewById(R.id.quote);
                // TODO:  Get the cursor from saved fragment
                mQuoteText.setText(quote);
                mShareActionProvider.setShareIntent(getDefaultIntent());
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
//        intent.putExtra(Intent.EXTRA_TEXT, "Test shareIntent");

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

    }
}
