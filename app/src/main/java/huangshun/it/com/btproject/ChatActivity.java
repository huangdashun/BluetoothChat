package huangshun.it.com.btproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import huangshun.it.com.btproject.UI.ChatListViewAdapter;
import huangshun.it.com.btproject.UI.DrawerHScrollView;
import huangshun.it.com.btproject.sound.SoundEffect;
import huangshun.it.com.btproject.task.Task;
import huangshun.it.com.btproject.task.TaskService;
import huangshun.it.com.btproject.utils.Utils;

public class ChatActivity extends Activity implements View.OnClickListener{
	private final String TAG = "ChatActivity";
	public static int sAliveCount = 0;
	public static final String EXTRA_MESSAGER = "cn.com.farsgiht.bluetoothdemo.BUNDLE";
	public static final String DEVICE_NAME = "device_name";
	// 蓝牙状态变量
	private static int sBTState = -1;
	
	private final int REQUES_BT_ENABLE_CODE = 123;
	private final int REQUES_SELECT_BT_CODE = 222;
	
	private ListView mList;
	private EditText mInput;
	private Button mSendBtn;
	private ImageView mEmoButton;
	private GridView mGridView;
	private boolean isUpdate = false;
	private BluetoothDevice mRemoteDevice;
	
	private LinearLayout mRootLayout, mChatLayout;
	
	private View mEmoView;
	private boolean isShowEmo = false;
	private boolean isHaspressed = false;
	private int mScrollHeight;
	
	private DrawerHScrollView mScrollView;
	private ChatListViewAdapter mAdapter2;
	private ArrayList<HashMap<String, Object>> mChatContent2 = new ArrayList<HashMap<String, Object>>();
	private BluetoothAdapter mBluetoothAdapter;
	
	private ArrayList<HashMap<String, Object>> mEmoList = new ArrayList<HashMap<String, Object>>();
	 // 已连接设备的名字
    private String mConnectedDeviceName = null;
    
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
            //设置聊天信息的时间
			SimpleDateFormat df0 = new SimpleDateFormat("MM-dd HH:mm:ss");
			String pdate=df0.format(System.currentTimeMillis()).toString();		    
			switch(msg.what){
			case -1:
				showToast("没有连接其它用户，点击\"Menu\"扫描并选择周国用户");
				SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
				break;
			case Task.TASK_SEND_MSG:
//			   showToast(msg.obj.toString());
		       String writeMessage = msg.obj.toString();
		       if(writeMessage!=null && isHaspressed)
			   {
			       //将发送的信息插入到数据库
			       ContentValues values=new ContentValues();
			       values.put("name", "我");
			       values.put("pdate",pdate);
			       values.put("informations", writeMessage);
			       //创建数据库
			       DatabaseHelper insertdbHelper=new DatabaseHelper(ChatActivity.this,"zhsf_db");
			       SQLiteDatabase insertdb=insertdbHelper.getWritableDatabase();
			       insertdb.insert("info", null, values);
		       }
				if(sAliveCount <= 0){
					Utils.notifyMessage(ChatActivity.this, msg.obj.toString(), ChatActivity.this);
				}			  
				break;	
			case Task.TASK_RECV_MSG:	
			   String readMessage =((HashMap<String,Object>) msg.obj).get(ChatListViewAdapter.KEY_TEXT).toString();
			   mConnectedDeviceName = ((HashMap<String,Object>) msg.obj).get(ChatListViewAdapter.KEY_NAME).toString(); 
			   if(readMessage!=null)
			   {
				   //将接受的信息插入到数据库
				   ContentValues values2=new ContentValues();
				   values2.put("name", mConnectedDeviceName);
				   values2.put("pdate",pdate);
				   values2.put("informations", readMessage);
				   DatabaseHelper insertdbHelper2=new DatabaseHelper(ChatActivity.this,"zhsf_db");
				   SQLiteDatabase insertdb2=insertdbHelper2.getWritableDatabase();
				   insertdb2.insert("info", null, values2);         
			   }
				
				if(msg.obj == null)
					return;
				if(msg.obj instanceof HashMap<?, ?>){
					showTargetMessage((HashMap<String, Object>) msg.obj);					
				}		
				if(sAliveCount <= 0){
					Utils.notifyMessage(ChatActivity.this, "您有未读取消息", ChatActivity.this);
				}							  
				break;
			case Task.TASK_GET_REMOTE_STATE:
				setTitle((String)msg.obj);
				if(sAliveCount <= 0){
					if(isBTStateChanged(msg.arg1) && msg.arg1 != TaskService.BT_STAT_WAIT)
						Utils.notifyMessage(ChatActivity.this, (String)msg.obj, ChatActivity.this);
				}
				break;
	
			}
		}
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		// 获得蓝牙管理器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Your device is not support Bluetooth!");
			Toast.makeText(this, "该设备没有蓝牙设备", Toast.LENGTH_LONG).show();
			return;
		}

		/**
		 * 安卓从6.0开始有了运行时权限
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Android M Permission check
			if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
			}
		}

		mRootLayout = (LinearLayout) findViewById(R.id.root);
		mChatLayout = (LinearLayout) findViewById(R.id.topPanel);
		mList = (ListView) findViewById(R.id.listView1);
	
		mAdapter2 = new ChatListViewAdapter(this, mChatContent2);
		
		mList.setAdapter(mAdapter2);

		// 初始化表情
		mEmoView = initEmoView();
		
		mInput = (EditText) findViewById(R.id.inputEdit);
		mInput.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击输入框后，隐藏表情，显示输入法
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.showSoftInput(mInput, 0);
				showEmoPanel(false);
			}
		});
		
		mSendBtn = (Button) findViewById(R.id.sendBtn);
		mEmoButton = (ImageView) findViewById(R.id.emotionBtn);
		
		mSendBtn.setOnClickListener(this);
		mEmoButton.setOnClickListener(this);
		
		//---------------------------------------------------------------------
		// 打开蓝牙设备
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE); 
		}else{
			// 默认设备作为服务端
			startServiceAsServer();
		}
		//---------------------------------------------------------------------
	}
	
	private View initEmoView(){
		if(mEmoView == null){
			LayoutInflater inflater = getLayoutInflater();
			mEmoView = inflater.inflate(R.layout.emo_layout, null);
			
			mScrollView = (DrawerHScrollView) mEmoView.findViewById(R.id.scrollView);
			mGridView = (GridView) mEmoView.findViewById(R.id.gridView);
			mGridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
	                // 在android中要显示图片信息，必须使用Bitmap位图的对象来装载  
	                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), (Integer) mEmoList.get(position).get("img")); 
					ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap);  
	                SpannableString spannableString = new SpannableString((String) mEmoList.get(position).get("text"));//face就是图片的前缀名  
	                spannableString.setSpan(imageSpan, 0, 8,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
	                mInput.append(spannableString);
	                System.out.println("mInput:"+mInput.getText());
				}
			});

	        mScrollHeight = setScrollGridView(mScrollView, mGridView, 3);
	        System.out.println("mScrollHeight:" + mScrollHeight);
		}
		return mEmoView;
	}
	

	
	private void startServiceAsServer(){
		TaskService.start(this, mHandler);
		TaskService.newTask(new Task(mHandler, Task.TASK_START_ACCEPT, null));
		SoundEffect.getInstance(this).play(SoundEffect.SOUND_PLAY);
	}
	
	@Override
	protected void onResume() {
		sAliveCount++;
		super.onResume();
	}

	@Override
	protected void onPause() {
		sAliveCount--;
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭蓝牙
		if(mBluetoothAdapter.isEnabled())
			mBluetoothAdapter.disable();
		// 停止服务
		TaskService.stop(this);
	}
	
	
	@Override
	public void onClick(View v) {
		if(v == mSendBtn){
			String msg = mInput.getText().toString().trim();
			TaskService.newTask(new Task(mHandler, Task.TASK_GET_REMOTE_STATE, null));//通过点击按钮触发相应线程的启动，比较巧妙，值得学习
			if(msg.length() == 0){
				showToast("聊天内容为空");
				SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
				return;
			}
			
			//------ DEUBG ------ 
			TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{msg}));
			showOwnMessage(msg);//立马显示自己发送的消息，所以在handler里面就没有再做处理
			isHaspressed = true;//数据库可以开始记录消息啦
			mInput.setText("");
		}else if(v == mEmoButton){
			System.out.println("Emo btn clicked");
			// 关闭输入法
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(mInput.getWindowToken(),0);
			if(isShowEmo){
				showEmoPanel(false);
			}else{
				showEmoPanel(true);
			}
		}
	}
	
	private void showEmoPanel(boolean show){
		if(show && !isShowEmo){
			mEmoView.setVisibility(View.VISIBLE);
			mEmoButton.setImageResource(R.drawable.emo_collapse);
			ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
			params.height = mChatLayout.getHeight() - mScrollHeight;
			mChatLayout.setLayoutParams(params);
			isShowEmo = true;
		}else if(!show && isShowEmo){
			mEmoView.setVisibility(View.GONE);
			mEmoButton.setImageResource(R.drawable.emo_bkg);
			ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
			params.height = mChatLayout.getHeight() + mScrollHeight;
			mChatLayout.setLayoutParams(params);
			isShowEmo = false;
		}
		if(!isUpdate && show){
			LayoutParams para = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			mRootLayout.addView(mEmoView, para);
			isUpdate = true;
		}
	}
	
	
	
	private boolean isBTStateChanged(int now){
		if(sBTState != now){
			sBTState = now;
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 显示对方信息
	 * @param data
	 */
	private void showTargetMessage(HashMap<String, Object> data){
		SimpleDateFormat df1 = new SimpleDateFormat("E MM月dd日   HH:mm ");
		data.put(ChatListViewAdapter.KEY_DATE, df1.format(System.currentTimeMillis()).toString());
		data.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
		mChatContent2.add(data);
		mAdapter2.notifyDataSetChanged();
		SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_RECV);
	}
	
	/**
	 * 显示自己信息
	 * @param data
	 */
	private void showOwnMessage(String msg){
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(ChatListViewAdapter.KEY_ROLE, ChatListViewAdapter.ROLE_OWN);//哪个角色的消息
		map.put(ChatListViewAdapter.KEY_NAME, mBluetoothAdapter.getName());
		map.put(ChatListViewAdapter.KEY_TEXT, msg);
		SimpleDateFormat df2 = new SimpleDateFormat("E MM月dd日  HH:mm ");
		map.put(ChatListViewAdapter.KEY_DATE, df2.format(System.currentTimeMillis()).toString());
		map.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
		mChatContent2.add(map);
		mAdapter2.notifyDataSetChanged();
		SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_SEND);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.option_menu, menu);
	     return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		  switch (item.getItemId()) {
	        case R.id.scan:
	        	startActivityForResult(new Intent(this, SelectDevice.class), REQUES_SELECT_BT_CODE);
				break;
	        case R.id.discoverable:
	            // 调用设置用户名方法
	        	AlertDialog.Builder dlg = new AlertDialog.Builder(this);
				final EditText devNameEdit = new EditText(this);
				dlg.setView(devNameEdit);
				dlg.setTitle("请输入用户名");
				dlg.setPositiveButton("设置", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if(devNameEdit.getText().toString().length() != 0)
							mBluetoothAdapter.setName(devNameEdit.getText().toString());
					}
				});
				dlg.create();
				dlg.show();
	            return true;
	        case R.id.record:
	        	Intent recordIntent = new Intent(ChatActivity.this, RecordListActivity.class);
	            startActivity(recordIntent);
	            return true;
	        case R.id.exit:
//	        	Intent aboutIntent = new Intent(ChatActivity.this, DownloadActivity.class);
//	            startActivity(aboutIntent);
				ensureDiscoverable();
	            return true;
	        }
	        return false;
	}
	/**
	 * 让本设备可见
	 */
	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUES_BT_ENABLE_CODE && resultCode == RESULT_OK){
			startServiceAsServer();
		}else if(requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK){
			mRemoteDevice = data.getParcelableExtra("DEVICE");
			if(mRemoteDevice == null)
				return;
			TaskService.newTask(new Task(mHandler, Task.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void showToast(String msg){
		Toast tst = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		tst.setGravity(Gravity.CENTER | Gravity.TOP, 0, 240);
		tst.show();
	}
	
	
	// 设置表情的多页滚动显示控件
	public int setScrollGridView(DrawerHScrollView scrollView, GridView gridView, 
			int lines) {
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
		Display display = getWindowManager().getDefaultDisplay();
        System.out.println("Width:" + display.getWidth());
        System.out.println("Height:" + display.getHeight());

		
		int scrollWid = display.getWidth();
		int scrollHei;
		System.out.println("scrollWid:" + scrollWid);
		if (scrollWid <= 0 ){
			Log.d(TAG, "scrollWid or scrollHei is less than 0");
			return 0;
		}
		 
		  
		float density  = dm.density;      // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		
		int readlViewWidht = 56;
		// 图片都放在了Hdpi中，所以计算出图片的像素独立宽度
		int viewWidth = (int) (readlViewWidht * density / 1.5);
		int viewHeight = viewWidth;
		System.out.println("viewWidth:" + viewWidth + " viewHeight:" + viewHeight);
		
		int numColsPage = scrollWid / viewWidth;
		int spaceing = (scrollWid - viewWidth * numColsPage)/(numColsPage);
		System.out.println("Space:" + spaceing);


		SimpleAdapter adapter = getEmoAdapter();
		int pages = adapter.getCount() / (numColsPage * lines);
		
		if (pages * numColsPage * lines < adapter.getCount()){
			pages++;
		}

		System.out.println("pages:" + pages);
		
		scrollHei = lines * viewHeight + spaceing * (lines + 1);
		
		LayoutParams params = new LayoutParams(pages * scrollWid, LayoutParams.WRAP_CONTENT);
		gridView.setLayoutParams(params);
		gridView.setColumnWidth(viewWidth);
		gridView.setHorizontalSpacing(spaceing);
		gridView.setVerticalSpacing(spaceing);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setNumColumns(numColsPage * pages);

		//adapter = new DrawerListAdapter(this, colWid, colHei);
		//listener = new DrawerItemClickListener();
		gridView.setAdapter(adapter);
		//mGridView.setOnItemClickListener(listener);

		scrollView.setParameters(pages, 0, scrollWid, spaceing);
		//updateDrawerPageLayout(pageNum, 0);
		// 表情区域还要加上分布显示区
		int pageNumHei = (int) (18 * density); 
		return scrollHei + pageNumHei;
	}
	
		
	private SimpleAdapter getEmoAdapter(){
		   HashMap<String, Object> map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo001);
		   map.put("text", "<emo001>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo002);
		   map.put("text", "<emo002>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo003);
		   map.put("text", "<emo003>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo004);
		   map.put("text", "<emo004>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo005);
		   map.put("text", "<emo005>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo006);
		   map.put("text", "<emo006>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo007);
		   map.put("text", "<emo007>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo008);
		   map.put("text", "<emo008>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo009);
		   map.put("text", "<emo009>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo010);
		   map.put("text", "<emo010>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo011);
		   map.put("text", "<emo011>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo012);
		   map.put("text", "<emo012>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo013);
		   map.put("text", "<emo013>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo014);
		   map.put("text", "<emo014>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo015);
		   map.put("text", "<emo015>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo016);
		   map.put("text", "<emo016>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo017);
		   map.put("text", "<emo017>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo018);
		   map.put("text", "<emo018>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo019);
		   map.put("text", "<emo019>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo020);
		   map.put("text", "<emo020>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo021);
		   map.put("text", "<emo021>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo022);
		   map.put("text", "<emo022>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo023);
		   map.put("text", "<emo023>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo024);
		   map.put("text", "<emo024>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo025);
		   map.put("text", "<emo025>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo026);
		   map.put("text", "<emo026>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo027);
		   map.put("text", "<emo027>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo028);
		   map.put("text", "<emo028>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo029);
		   map.put("text", "<emo029>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo030);
		   map.put("text", "<emo030>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo031);
		   map.put("text", "<emo031>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo032);
		   map.put("text", "<emo032>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo033);
		   map.put("text", "<emo033>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo034);
		   map.put("text", "<emo034>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo035);
		   map.put("text", "<emo035>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo036);
		   map.put("text", "<emo036>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo037);
		   map.put("text", "<emo037>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo038);
		   map.put("text", "<emo038>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo039);
		   map.put("text", "<emo039>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo040);
		   map.put("text", "<emo040>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo041);
		   map.put("text", "<emo041>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo042);
		   map.put("text", "<emo042>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo043);
		   map.put("text", "<emo043>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo044);
		   map.put("text", "<emo044>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo045);
		   map.put("text", "<emo045>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo046);
		   map.put("text", "<emo046>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo047);
		   map.put("text", "<emo047>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo048);
		   map.put("text", "<emo048>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo049);
		   map.put("text", "<emo049>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo050);
		   map.put("text", "<emo050>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo051);
		   map.put("text", "<emo051>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo052);
		   map.put("text", "<emo052>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo053);
		   map.put("text", "<emo053>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo054);
		   map.put("text", "<emo054>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo055);
		   map.put("text", "<emo055>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo056);
		   map.put("text", "<emo056>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo057);
		   map.put("text", "<emo057>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo058);
		   map.put("text", "<emo058>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo059);
		   map.put("text", "<emo059>");
		   mEmoList.add(map);
		   map = new HashMap<String, Object>();
		   map.put("img", R.drawable.emo060);
		   map.put("text", "<emo060>");
		   mEmoList.add(map);
		   
		   /**
		    * 上述添加表情效率高，但是代码太冗余，下面的方式代码简单，但是效率较低
		    */
		   /*
		   HashMap<String, Integer> map;
		   for(int i = 0; i < 100; i++){
			   map = new HashMap<String, Integer>();
			   Field field=R.drawable.class.getDeclaredField("image"+i);  
			   int resourceId=Integer.parseInt(field.get(null).toString());
			   map.put("img", resourceId);
			   mEmoList.add(map);
		   }
		   */
		return new SimpleAdapter(this, mEmoList, R.layout.grid_view_item, 
					new String[]{"img"}, new int[]{R.id.imageView});
	}
	
	
    long waitTime = 2000;
    long touchTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
			long currentTime = System.currentTimeMillis();
			if((currentTime-touchTime)>=waitTime) {
				Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
				touchTime = currentTime;
			}else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}


