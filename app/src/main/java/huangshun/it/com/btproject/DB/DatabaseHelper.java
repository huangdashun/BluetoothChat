package huangshun.it.com.btproject.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hs on 2017/3/20.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;

    public DatabaseHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DatabaseHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    public DatabaseHelper(Context context, String name, CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    public void onCreate(SQLiteDatabase db) {
        System.out.println("Create a Database");
        db.execSQL("create table info(name varchar(20),information varchar(50),date varchar(50))");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("Update a Database");
    }
}
