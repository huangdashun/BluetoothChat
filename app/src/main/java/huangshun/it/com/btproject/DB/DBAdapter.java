package huangshun.it.com.btproject.DB;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import huangshun.it.com.btproject.bean.User;

/**
 * @author Login
 */
public class DBAdapter {
    public static final String DB_ACTION = "db_action";//LogCat

    private static final String DB_NAME = "user.db";//数据库名
    private static final String DB_TABLE = "userinfo";//数据库表名
    private static final int DB_VERSION = 1;//数据库版本号

    public static final String KEY_ID = "_id";  //表属性ID
    public static final String KEY_USERNAME = "username";//表属性username
    public static final String KEY_PASSWORD = "password";//表属性password

    private SQLiteDatabase db;
    private Context xContext;
    private DBOpenHelper dbOpenHelper;

    public DBAdapter(Context context) {
        xContext = context;
    }

    /**
     * 空间不够存储的时候设为只读
     *
     * @throws SQLiteException
     */
    public void open() throws SQLiteException {
        dbOpenHelper = new DBOpenHelper(xContext, DB_NAME, null, DB_VERSION);
        try {
            db = dbOpenHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbOpenHelper.getReadableDatabase();
        }
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    /**
     * 向表中添加一条数据
     *
     * @param user
     * @return
     */
    public long insert(User user) {
        ContentValues newValues = new ContentValues();

        newValues.put(KEY_USERNAME, user.username);
        newValues.put(KEY_PASSWORD, user.password);


        return db.insert(DB_TABLE, null, newValues);
    }

    /**
     * 删除一条数据
     *
     * @param id
     * @return
     */
    public long deleteOneData(long id) {
        return db.delete(DB_TABLE, KEY_ID + "=" + id, null);
    }

    /**
     * 删除所有数据
     *
     * @return
     */
    public long deleteAllData() {
        return db.delete(DB_TABLE, null, null);
    }

    /**
     * 根据id查询数据的代码
     *
     * @param id
     * @return
     */
    public User[] queryOneData(long id) {
        Cursor result = db.query(DB_TABLE, new String[]{KEY_ID, KEY_USERNAME, KEY_PASSWORD},
                KEY_ID + "=" + id, null, null, null, null);
        return ConvertTouser(result);
    }

    /**
     * 根据用户名查询数据的代码
     *
     * @param username
     * @return
     */
    public User[] queryuserData(String username) {
        Cursor result = db.query(DB_TABLE, new String[]{KEY_ID, KEY_USERNAME, KEY_PASSWORD},
                KEY_USERNAME + "=" + "'" + username + "'", null, null, null, null);
        return ConvertTouser(result);
    }

    /**
     * 查询全部数据的代码
     *
     * @return
     */
    public User[] queryAllData() {
        Cursor result = db.query(DB_TABLE, new String[]{KEY_ID, KEY_USERNAME, KEY_PASSWORD},
                null, null, null, null, null);
        System.out.print(result);
        return ConvertTouser(result);
    }

    public long updateOneData(long id, User user) {
        ContentValues newValues = new ContentValues();

        newValues.put(KEY_USERNAME, user.username);
        newValues.put(KEY_PASSWORD, user.password);

        return db.update(DB_TABLE, newValues, KEY_ID + "=" + id, null);
    }

    private User[] ConvertTouser(Cursor cursor) {
        int resultCounts = cursor.getCount();
        if (resultCounts == 0 || !cursor.moveToFirst()) {
            return null;
        }
        User[] users = new User[resultCounts];
        Log.i(DB_ACTION, "user len:" + users.length);
        for (int i = 0; i < resultCounts; i++) {
            users[i] = new User();
            users[i].ID = cursor.getInt(0);
            users[i].username = cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
            users[i].password = cursor.getString(cursor.getColumnIndex(KEY_PASSWORD));
            Log.i(DB_ACTION, "user " + i + "info :" + users[i].toString());
            cursor.moveToNext();
        }
        return users;
    }


    /**
     * 静态Helper类，用于建立、更新和打开数据库
     */
    private static class DBOpenHelper extends SQLiteOpenHelper {
        /*
         * 手动建库代码
        CREATE TABLE userinfo
        (_id integer primary key autoincrement,
        name text not null,
        age integer,
        height float);*/
        private static final String DB_CREATE =
                "CREATE TABLE " + DB_TABLE
                        + " (" + KEY_ID + " integer primary key autoincrement, "
                        + KEY_USERNAME + " text not null, "
                        + KEY_PASSWORD + " text not null);";

        public DBOpenHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            Log.i(DB_ACTION, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            //函数在数据库需要升级时被调用，
            //一般用来删除旧的数据库表，
            //并将数据转移到新版本的数据库表中
            _db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(_db);
            Log.i(DB_ACTION, "Upgrade");
        }

    }
}
