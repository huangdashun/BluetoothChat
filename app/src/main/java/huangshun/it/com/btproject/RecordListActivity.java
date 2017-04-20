package huangshun.it.com.btproject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import huangshun.it.com.btproject.DB.DatabaseHelper;
import huangshun.it.com.btproject.utils.EmoContentUtil;

public class RecordListActivity extends Activity {
    private ArrayAdapter<CharSequence> queryArrayAdapter;
    private ListView queryListView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.record_list);
        DatabaseHelper dbHelper = new DatabaseHelper(RecordListActivity.this, "record_db");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor cursor=db.query("info",new String[]{"name","informations","pdate"},"name=?",new String[]{"zhouzhou"}, null,null,null);
        Cursor cursor = db.query("info", new String[]{"name", "information", "date"}, null, null, null, null, null);
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

        Button returnButton = (Button) findViewById(R.id.return_button);
        returnButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent recordIntent = new Intent(RecordListActivity.this, ChatActivity.class);
                startActivity(recordIntent);
            }
        });
    }


}
