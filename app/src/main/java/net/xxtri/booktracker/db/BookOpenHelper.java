package net.xxtri.booktracker.db; /**
 * Created by trinity on 9/30/15.
 */


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.xxtri.booktracker.Book;


public class BookOpenHelper extends SQLiteOpenHelper {

    //version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "booksManager";

    //Table Name
    private static final String TABLE_BOOKS = "books";

    //Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ISBN = "string";
    private static final String KEY_READ = "read";
    private static final String KEY_PROGRESS = "progress";



    private static final String BOOK_TABLE_CREATE =
            "CREATE TABLE " +  TABLE_BOOKS + " (" +  KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_ISBN + " TEXT, "
                    + KEY_READ + "REAL"
                    + KEY_PROGRESS + " REAL);";



    BookOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BOOK_TABLE_CREATE);
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);

        // Create tables again
        onCreate(db);
    }


    public void addBook(Book book){

    }

}
