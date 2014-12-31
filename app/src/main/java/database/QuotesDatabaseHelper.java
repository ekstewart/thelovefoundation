package database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.avadio.android.ilove.app.MyApplication;

import datamodel.QUOTESTABLE;

/**
 * Created by ljxi_828 on 5/30/14.
 */
public class QuotesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quotes.db";
    private static final int DATABASE_VERSION = 3;
    private Context mContext;

    public QuotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        QUOTESTABLE.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        QUOTESTABLE.onUpgrade(database, oldVersion, newVersion);

        SharedPreferences.Editor editor = mContext.getSharedPreferences(MyApplication.MY_PREFS_NAME, mContext.MODE_PRIVATE).edit();
        editor.putBoolean("quotesLoaded", false);
        editor.commit();
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
//    private boolean checkDataBase(){
//        SQLiteDatabase checkDB = null;
//
//        try{
//            File myPath = mContext.getDatabasePath(DATABASE_NAME);
//            checkDB = SQLiteDatabase.openDatabase(myPath.getPath(), null, SQLiteDatabase.OPEN_READONLY);
//
//        }catch(SQLiteException e){
//            //database does't exist yet.
//        }
//
//        if(checkDB != null){
//            checkDB.close();
//        }
//
//        return checkDB != null ? true : false;
//    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
//    private void copyDataBase() throws IOException {
//
//        //Open your local db as the input stream
//        InputStream myInput = mContext.getAssets().open(DATABASE_NAME);
//
//        // Path to the just created empty db
//        String outFileName = DB_PATH + DATABASE_NAME;
//        File myPath = mContext.getDatabasePath(DATABASE_NAME);
//
//
//        //Open the empty db as the output stream
//        OutputStream myOutput = new FileOutputStream(myPath);
//
//        //transfer bytes from the inputfile to the outputfile
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = myInput.read(buffer))>0){
//            myOutput.write(buffer, 0, length);
//        }
//
//        //Close the streams
//        myOutput.flush();
//        myOutput.close();
//        myInput.close();
//    }
}
