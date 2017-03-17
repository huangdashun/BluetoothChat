package huangshun.it.com.btproject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import huangshun.it.com.btproject.DB.DatabaseHelper;

public class RecordListActivity extends Activity{
	private ArrayAdapter<CharSequence> queryArrayAdapter;
	private ListView queryListView;
	private DisplayMetrics dm;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list);
        
        dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
        
        DatabaseHelper dbHelper=new DatabaseHelper(RecordListActivity.this,"zhsf_db");
		SQLiteDatabase db=dbHelper.getReadableDatabase();
		//Cursor cursor=db.query("info",new String[]{"name","informations","pdate"},"name=?",new String[]{"zhouzhou"}, null,null,null);
		Cursor cursor=db.query("info",new String[]{"name","informations","pdate"},null,null, null,null,null);
		queryArrayAdapter = new ArrayAdapter<CharSequence >(RecordListActivity.this, R.layout.information);
		queryListView = (ListView) findViewById(R.id.query);
		queryListView.setAdapter(queryArrayAdapter);
	    while(cursor.moveToNext()){
			String name=cursor.getString(cursor.getColumnIndex("name"));
			String informations=cursor.getString(cursor.getColumnIndex("informations"));
			String pdate=cursor.getString(cursor.getColumnIndex("pdate"));
			System.out.print("@name--->"+name);
		    System.out.print("@informations--->"+informations);
			System.out.println("@pdate--->"+pdate);
			queryArrayAdapter.add(name+"    "+pdate);
			Spanned spann2 = makeChatContent(informations);
			queryArrayAdapter.add(spann2);
			
	    }
        
        Button returnButton=(Button)findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent recordIntent = new Intent(RecordListActivity.this,ChatActivity.class);
                startActivity(recordIntent);  
            }
        });
	}
	
	//<emo001]
	private Spanned makeChatContent(String msg){
		String htmlStr = msg;
		while(true){
			int start = htmlStr.indexOf("<emo", 0);
			if(start != -1){
				String resIdStr = htmlStr.substring(start+1,start + 7);
				htmlStr = htmlStr.replaceFirst("<emo...>", "<img src='" + resIdStr +"'/>");
			}else{
				return Html.fromHtml(htmlStr, imgGetter, null);
			}
		}
	}
	
	private ImageGetter imgGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			int resID =  getResources().getIdentifier(source, "drawable", getPackageName());
			Drawable drawable = getResources().getDrawable(resID);
			int w = (int) (drawable.getIntrinsicWidth() * dm.density / 2);
			int h = (int) (drawable.getIntrinsicHeight() * dm.density / 2);
			drawable.setBounds(0, 0, w , h);
			return drawable;
		}
	};
	
}
