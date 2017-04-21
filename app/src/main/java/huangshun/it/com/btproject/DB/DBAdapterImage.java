package huangshun.it.com.btproject.DB;

import java.sql.Blob;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * @author Login
 *
 */
public class DBAdapterImage
{
    public static final String DB_ACTION="db_action";//LogCat
    
    private static final String DB_NAME="imageview.db";//数据库名
    private static final String DB_TABLE="imageviewinfo";//数据库表名
    private static final int    DB_VERSION=1;//数据库版本号
    
    public static final String KEY_ID = "_id";  //表属性ID
    public static final Blob image=null;
    private SQLiteDatabase db ;
    private Context xContext ;
    private DBOpenHelper dbOpenHelper ;
    public DBAdapterImage(Context context)
    {
        xContext = context ;
    }
    
    /** 空间不够存储的时候设为只读
     * @throws SQLiteException
     */
    public void open() throws SQLiteException
    {
        dbOpenHelper = new DBOpenHelper(xContext, DB_NAME, null,DB_VERSION);
        try
        {
            db = dbOpenHelper.getWritableDatabase();
        }
        catch (SQLiteException e)
        {
            db = dbOpenHelper.getReadableDatabase();
        }
    }
    
    public void close()
    {
        if(db != null)
        {
            db.close();
            db = null;
        }
    }
    /**
     * 向表中添加一条数据
     * @param user
     * @return
     */
    public long insert(String DB_TABLE,ContentValues values)
    {
   
        return db.insert(DB_TABLE, null, values);
    }
    public Cursor queryAllData()
    {
        Cursor result = db.query(DB_TABLE, null,null, null, null, null,null);
        if (result.moveToFirst())  
        {  
        result.getBlob(result.getColumnIndex("image"));
        return result;
        }  
        result.close();
		return null;  
    }
  
    /**
     * 删除所有数据
     * @return
     */
    public long deleteAllData()
    {
        return db.delete(DB_TABLE, null, null);
    }

    
   
    
 
    
    /**
     * 静态Helper类，用于建立、更新和打开数据库
     */
    private static class DBOpenHelper extends SQLiteOpenHelper
    {
        /*
         * 手动建库代码
        CREATE TABLE userinfo
        (_id integer primary key autoincrement,
        name text not null,
        age integer,
        height float);*/
        public DBOpenHelper(Context context, String name,
                CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	db.execSQL("Create table imageviewinfo ( _id INTEGER PRIMARY KEY AUTOINCREMENT,image BLOB );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion)
        {
            //函数在数据库需要升级时被调用，
            //一般用来删除旧的数据库表，
            //并将数据转移到新版本的数据库表中
            _db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE);
            onCreate(_db);
            Log.i(DB_ACTION, "Upgrade");
        }

    }
}
