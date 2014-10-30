package database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.apache.http.auth.AUTH;

import java.util.Arrays;
import java.util.HashSet;

import datamodel.QUOTESTABLE;

public class MyContentProvider extends ContentProvider {
    public MyContentProvider() {
    }

    // Database
    private QuotesDatabaseHelper mQuotesDatabaseHelper;

    // Used for the UriMatcher
    private static final int QUOTES = 10;
    private static final int QUOTE_ID = 20;

    private static final String AUTHORITY = "com.avadio.quotes.contentprovider";

    private static final String BASE_PATH = "quotes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/quotes";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "quote";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, QUOTES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", QUOTE_ID);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mQuotesDatabaseHelper.getWritableDatabase();
        int rowsDeleted = 0;

        long id = 0;
        switch (uriType) {
            case QUOTES:
                id = sqlDB.insert(QUOTESTABLE.TABLE_QUOTES, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        //TODO:  will cause onLoadFinished to fire for every entry
        //getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public boolean onCreate() {
        mQuotesDatabaseHelper = new QuotesDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(QUOTESTABLE.TABLE_QUOTES);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case QUOTES:
                break;
            case QUOTE_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(QUOTESTABLE.COL_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = mQuotesDatabaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Make sure listeners are notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void checkColumns(String[] projection) {
        String[] available = {QUOTESTABLE.COL_ID, QUOTESTABLE.COL_QUOTE, QUOTESTABLE.COL_AUTHOR};
        if (projection != null) {
            HashSet<String> reqColumns = new HashSet<String>(Arrays.asList(projection));

            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

            if (!availableColumns.containsAll(reqColumns)) {
                throw new IllegalArgumentException("unknown columns in projection:  " + reqColumns);
            }
        }
    }
}
