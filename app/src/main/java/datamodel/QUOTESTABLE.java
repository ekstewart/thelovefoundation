package datamodel;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by ljxi_828 on 5/30/14.
 */
public class QUOTESTABLE {
    private static final String TAG = "QUOTETABLE";

    // Table and Columns
    public static final String TABLE_QUOTES = "quotes";
    public static final String COL_ID = "_id";

    public static final String COL_AUTHOR = "author";
    public static final String COL_QUOTE = "quote";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_QUOTES
            + "("
            + COL_ID + " integer primary key autoincrement, "
            + COL_AUTHOR + " text not null, "
            + COL_QUOTE + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS  " + TABLE_QUOTES);
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS  " + TABLE_QUOTES);
        onCreate(database);
    }
}
