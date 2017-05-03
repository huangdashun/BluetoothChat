package huangshun.it.com.btproject.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import huangshun.it.com.btproject.DB.DatabaseHelper;
import huangshun.it.com.btproject.Model.Emoji;
import huangshun.it.com.btproject.Model.Task;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.service.TaskService;
import huangshun.it.com.btproject.sound.SoundEffect;
import huangshun.it.com.btproject.utils.NotificationUtil;
import huangshun.it.com.btproject.utils.ToastUtil;
import huangshun.it.com.btproject.view.ChatListViewAdapter;

import static huangshun.it.com.btproject.Model.Task.TASK_SEND_MSG_FAIL;

/**
 * Created by hs on 2017/3/27.
 */
public class ChatActivity extends Activity implements View.OnClickListener {
    private final String TAG = "ChatActivity";
    public static int sAliveCount = 0;
    public static final String DEVICE_NAME = "device_name";
    // 蓝牙状态变量
    private static int sBTState = -1;

    private final int REQUES_BT_ENABLE_CODE = 001; //使能蓝牙
    private final int REQUES_SELECT_BT_CODE = 002;//选中设备进行连接
    //表情包
    private View mEmoView;
    private Emoji mEmoji;

    private ListView mList;
    private EditText mInput;
    private Button mSendBtn;
    private ImageView mEmoButton;
    private TextView mTvState;
    private TextView mTvTitle;
    private ImageButton mImageAddButton;
    private ImageButton mImageSearchButton;
    private BluetoothDevice mRemoteDevice;

    private LinearLayout mRootLayout, mChatLayout;
    private boolean isUpdate = false;//用来第一次加载表情view
    private boolean isShowEmo = false;//是否已经展示表情包
    private boolean isMaySave = false;//是否可以将聊天记录存入数据库


    private ChatListViewAdapter mChatListAdapter;
    private ArrayList<HashMap<String, Object>> mChatContentData = new ArrayList<>();//存放聊天内容的集合
    private BluetoothAdapter mBluetoothAdapter;


    // 已连接设备的名字
    private String mConnectedDeviceName = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //设置聊天信息的时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
            String date = simpleDateFormat.format(System.currentTimeMillis()).toString();
            switch (msg.what) {
                case TASK_SEND_MSG_FAIL://当没有连接用户的时候
//                    ToastUtil.show(ChatActivity.this, "没有连接其它用户，点击\"Menu\"扫描并选择周国用户");
//                    SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
//                    break;
                case Task.TASK_SEND_MSG://发送信息
//			   showToast(msg.obj.toString());
                    String writeMessage = msg.obj.toString();

                    if (writeMessage != null && isMaySave) {
                        //将发送的信息插入到数据库
                        ContentValues values = new ContentValues();
                        values.put("name", "我");
                        values.put("date", date);
                        values.put("information", writeMessage);
                        //创建数据库
                        DatabaseHelper insertDbHelper = new DatabaseHelper(ChatActivity.this, "record_db");
                        SQLiteDatabase insertDb = insertDbHelper.getWritableDatabase();
                        insertDb.insert("info", null, values);
                    }
                    if (sAliveCount <= 0) {
                        Log.i(TAG, "sAliveCount:" + sAliveCount + " send_msg");
                        NotificationUtil.notifyMessage(ChatActivity.this, msg.obj.toString(), ChatActivity.this);
                    }
                    break;
                case Task.TASK_RECV_MSG:
                    String readMessage = ((HashMap<String, Object>) msg.obj).get(ChatListViewAdapter.KEY_TEXT).toString();
                    mConnectedDeviceName = ((HashMap<String, Object>) msg.obj).get(ChatListViewAdapter.KEY_NAME).toString();
                    if (readMessage != null) {
                        //将接收的信息插入到数据库
                        ContentValues values2 = new ContentValues();
                        values2.put("name", mConnectedDeviceName);
                        values2.put("date", date);
                        values2.put("information", readMessage);
                        DatabaseHelper insertDbHelper = new DatabaseHelper(ChatActivity.this, "record_db");
                        SQLiteDatabase insertDb = insertDbHelper.getWritableDatabase();
                        insertDb.insert("info", null, values2);
                    }

                    if (msg.obj == null)
                        return;
                    if (msg.obj instanceof HashMap<?, ?>) {
                        showTargetMessage((HashMap<String, Object>) msg.obj);
                    }
                    if (sAliveCount <= 0) {
                        NotificationUtil.notifyMessage(ChatActivity.this, "您有未读取消息", ChatActivity.this);
                    }
                    break;
                case Task.TASK_GET_REMOTE_STATE://获取远程连接的状态
                    if (((String) msg.obj).contains("正在连接")) {//防止提示信息过长
                        mTvTitle.setVisibility(View.GONE);
                    } else {
                        mTvTitle.setVisibility(View.VISIBLE);
                    }
                    mTvState.setText((String) msg.obj);//设置连接的状态
                    if (sAliveCount <= 0) {
                        if (isBTStateChanged(msg.arg1) && msg.arg1 != TaskService.BT_STAT_WAIT)
                            NotificationUtil.notifyMessage(ChatActivity.this, (String) msg.obj, ChatActivity.this);
                    }
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
        initListener();
        initBroadcast();
        // 获得蓝牙管理器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            Toast.makeText(this, "该设备不支持蓝牙设备", Toast.LENGTH_LONG).show();
            return;
        }

        /**
         * 安卓从6.0开始有了运行时权限
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android6.0运行时权限检查
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        // 打开蓝牙设备
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE);//使能设备
        } else {
            // 默认设备作为服务端
            startServiceAsServer();
        }
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("close.chat.activity");
        registerReceiver(mBroadcastReceiver, filter);
    }


    /**
     * 初始化view
     */
    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chat);
        mTvState = (TextView) findViewById(R.id.tv_state);//蓝牙连接状态
        mTvTitle = (TextView) findViewById(R.id.tv_title);//标题栏
        mRootLayout = (LinearLayout) findViewById(R.id.root);//根布局
        mChatLayout = (LinearLayout) findViewById(R.id.topPanel);
        mList = (ListView) findViewById(R.id.lv_chat);//聊天的列表
        mInput = (EditText) findViewById(R.id.inputEdit);//编辑框
        mSendBtn = (Button) findViewById(R.id.sendBtn);//发送按钮
        mEmoButton = (ImageView) findViewById(R.id.emotionBtn);//表情包按钮
        //弹出下拉框
        mImageAddButton = (ImageButton) findViewById(R.id.imb_top_add);
        //扫描
        mImageSearchButton = (ImageButton) findViewById(R.id.imb_top_search);
        //初始化表情类
        mEmoji = new Emoji(this);
        // 初始化表情
        mEmoView = mEmoji.initEmoView(mInput);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        mChatListAdapter = new ChatListViewAdapter(this, mChatContentData);//创建一个显示聊天内容的适配器
        mList.setAdapter(mChatListAdapter);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        mInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击输入框后，隐藏表情，显示输入法
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mInput, 0);//显示键盘
                showEmoPanel(false);//隐藏表情
            }
        });

        mSendBtn.setOnClickListener(this);
        mEmoButton.setOnClickListener(this);
        mImageAddButton.setOnClickListener(this);
        mImageSearchButton.setOnClickListener(this);
    }

    /**
     * 作为服务器开启服务
     */
    private void startServiceAsServer() {
        TaskService.start(this, mHandler);//初始化任务服务
        //向后台服务提交一个任务,作为服务器端监听远程的设备连接
        TaskService.newTask(new Task(mHandler, Task.TASK_START_ACCEPT, null));
        SoundEffect.getInstance(this).play(SoundEffect.SOUND_PLAY);//播放play的声音
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
        if (mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
        // 停止服务
        TaskService.stop(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendBtn://发送
                if (TaskService.state == TaskService.BT_STAT_ONLINE) {//并且如果是在线状态的话
                    String msg = mInput.getText().toString().trim();
                    TaskService.newTask(new Task(mHandler, Task.TASK_GET_REMOTE_STATE, null));//获取远程状态信息
                    if (msg.length() == 0) {//如果聊天内容为null
                        ToastUtil.show(ChatActivity.this, "聊天内容为空");
                        SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);//发出警告的声音
                        return;
                    }
                    //------ 开启发送信息的服务 ------
                    TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{msg}));
                    showOwnMessage(msg);//立马显示自己发送的消息，所以在handler里面就没有再做处理
                    isMaySave = true;//数据库可以开始记录消息啦
                    mInput.setText("");
                    mList.setSelection(mChatListAdapter.getCount());
                } else {
                    ToastUtil.show(ChatActivity.this, "没有连接其它用户，请先扫描并选择周围用户");
                    SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
                    break;
                }
                break;
            case R.id.emotionBtn://打开表情包
                System.out.println("Emo btn clicked");
                // 关闭输入法
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                if (isShowEmo) {
                    showEmoPanel(false);
                } else {
                    showEmoPanel(true);
                }
                break;
            case R.id.imb_top_add://弹出下拉框
                startActivity(new Intent(ChatActivity.this, PopDialogActivity.class));
                break;
            case R.id.imb_top_search://扫描设备
                startActivityForResult(new Intent(this, SelectDeviceActivity.class), REQUES_SELECT_BT_CODE);
                break;

        }


    }


    /**
     * 显示或隐藏表情面板
     *
     * @param show
     */
    private void showEmoPanel(boolean show) {
        int mScrollHeight = mEmoji.getScrollHeight();
        if (show && !isShowEmo) {
            mEmoView.setVisibility(View.VISIBLE);//显示表情包
            mEmoButton.setImageResource(R.drawable.emo_collapse);//修改表情面板按钮图片
            ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
            params.height = mChatLayout.getHeight() - mScrollHeight;
            mChatLayout.setLayoutParams(params);//重新设置布局的高度
            isShowEmo = true;
        } else if (!show && isShowEmo) {
            mEmoView.setVisibility(View.GONE);
            mEmoButton.setImageResource(R.drawable.emo_bkg);
            ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
            params.height = mChatLayout.getHeight() + mScrollHeight;
            mChatLayout.setLayoutParams(params);
            isShowEmo = false;
        }
        if (!isUpdate && show) {//第一次打开app的时候,将表情面板添加到rootview中
            LayoutParams para = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mRootLayout.addView(mEmoView, para);
            isUpdate = true;

        }
    }

    /**
     * 蓝牙状态是否改变
     *
     * @param now
     * @return
     */
    private boolean isBTStateChanged(int now) {
        if (sBTState != now) {
            sBTState = now;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 显示对方信息
     *
     * @param data
     */
    private void showTargetMessage(HashMap<String, Object> data) {
        SimpleDateFormat format = new SimpleDateFormat("E MM月dd日   HH:mm ");
        data.put(ChatListViewAdapter.KEY_DATE, format.format(System.currentTimeMillis()).toString());
        data.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        mChatContentData.add(data);
        mChatListAdapter.notifyDataSetChanged();
        mList.setSelection(mChatListAdapter.getCount());
        SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_RECV);
    }

    /**
     * 显示自己信息
     *
     * @param
     */
    private void showOwnMessage(String msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(ChatListViewAdapter.KEY_ROLE, ChatListViewAdapter.ROLE_OWN);//哪个角色的消息
        map.put(ChatListViewAdapter.KEY_NAME, mBluetoothAdapter.getName());//蓝牙名字
        map.put(ChatListViewAdapter.KEY_TEXT, msg);//聊天内容
        SimpleDateFormat dateFormat = new SimpleDateFormat("E MM月dd日  HH:mm ");
        map.put(ChatListViewAdapter.KEY_DATE, dateFormat.format(System.currentTimeMillis()).toString());
        map.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        mChatContentData.add(map);
        mChatListAdapter.notifyDataSetChanged();
        mList.setSelection(mChatListAdapter.getCount());
        SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_SEND);//开启发送信息的声音
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUES_BT_ENABLE_CODE && resultCode == RESULT_OK) {//使能蓝牙
            startServiceAsServer();
        } else if (requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK) {//选择设备进行连接
            mRemoteDevice = data.getParcelableExtra("DEVICE");
            if (mRemoteDevice == null)
                return;
            TaskService.newTask(new Task(mHandler, Task.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));//开启连接蓝牙的服务
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 按两次返回键才退出App
     */
    long waitTime = 2000;
    long touchTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - touchTime) >= waitTime) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                touchTime = currentTime;
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);
            ((Activity) context).finish();
        }
    };
}


