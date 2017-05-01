package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import huangshun.it.com.btproject.DB.DatabaseHelper;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.utils.EmoContentUtil;

public class RecordListActivity extends Activity {
    private ArrayAdapter<CharSequence> queryArrayAdapter;
    private ListView queryListView;
    private ImageView mBack;
    private ImageView mDelete;
    private SQLiteDatabase mDb;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.record_list);
        mBack = (ImageView) findViewById(R.id.im_back);
        mDelete = (ImageView) findViewById(R.id.im_delete);
        initData();
        initListener();

    }

    private void initData() {
        DatabaseHelper dbHelper = new DatabaseHelper(RecordListActivity.this, "record_db");
        mDb = dbHelper.getReadableDatabase();
        //Cursor cursor=db.query("info",new String[]{"name","informations","pdate"},"name=?",new String[]{"zhouzhou"}, null,null,null);
        Cursor cursor = mDb.query("info", new String[]{"name", "information", "date"}, null, null, null, null, null);
        queryArrayAdapter = new ArrayAdapter<>(RecordListActivity.this, R.layout.information);
        queryListView = (ListView) findViewById(R.id.lv_query);
        queryListView.setAdapter(queryArrayAdapter);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String information = cursor.getString(cursor.getColumnIndex("information"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            System.out.print("@name--->" + name);
            System.out.print("@information--->" + information);
            System.out.println("@date--->" + date);
            queryArrayAdapter.add(name + "    " + date);
            Spanned spann2 = EmoContentUtil.getInstance(this).getEmoContent(information);
            queryArrayAdapter.add(spann2);

        }
    }

    private void initListener() {
        mBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent recordIntent = new Intent(RecordListActivity.this, ChatActivity.class);
                startActivity(recordIntent);
            }
        });
        /**
         * 清空聊天记录
         */
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(RecordListActivity.this)
                        .setTitle("您确定要清空历史聊天记录吗")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDb.delete("info", null, null);
                                queryArrayAdapter.clear();
                                queryArrayAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();


            }
        });
    }
}
