package huangshun.it.com.btproject;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import huangshun.it.com.btproject.service.TaskService;
import huangshun.it.com.btproject.sound.SoundEffect;
import huangshun.it.com.btproject.utils.NotificationUtil;
import huangshun.it.com.btproject.utils.ToastUtil;
import huangshun.it.com.btproject.view.ChatListViewAdapter;

import static huangshun.it.com.btproject.Model.Task.TASK_SEND_MSG_FAIL;

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
    private ImageButton mImageAddButton;
    private ImageButton mImageSearchButton;

    private boolean isUpdate = false;
    private BluetoothDevice mRemoteDevice;

    private LinearLayout mRootLayout, mChatLayout;

    private boolean isShowEmo = false;
    private boolean isHaspressed = false;


    private ChatListViewAdapter mChatListAdapter;
    private ArrayList<HashMap<String, Object>> mChatContentData = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;


    // 已连接设备的名字
    private String mConnectedDeviceName = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //设置聊天信息的时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
            String pdate = simpleDateFormat.format(System.currentTimeMillis()).toString();
            switch (msg.what) {
                case TASK_SEND_MSG_FAIL://当没有连接用户的时候
//                    ToastUtil.show(ChatActivity.this, "没有连接其它用户，点击\"Menu\"扫描并选择周国用户");
//                    SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
//                    break;
                case Task.TASK_SEND_MSG:
//			   showToast(msg.obj.toString());
                    String writeMessage = msg.obj.toString();

                    if (writeMessage != null && isHaspressed) {
                        //将发送的信息插入到数据库
                        ContentValues values = new ContentValues();
                        values.put("name", "我");
                        values.put("pdate", pdate);
                        values.put("informations", writeMessage);
                        //创建数据库
                        DatabaseHelper insertdbHelper = new DatabaseHelper(ChatActivity.this, "zhsf_db");
                        SQLiteDatabase insertdb = insertdbHelper.getWritableDatabase();
                        insertdb.insert("info", null, values);
                    }
                    if (sAliveCount <= 0) {
                        NotificationUtil.notifyMessage(ChatActivity.this, msg.obj.toString(), ChatActivity.this);
                    }
                    break;
                case Task.TASK_RECV_MSG:
                    String readMessage = ((HashMap<String, Object>) msg.obj).get(ChatListViewAdapter.KEY_TEXT).toString();
                    mConnectedDeviceName = ((HashMap<String, Object>) msg.obj).get(ChatListViewAdapter.KEY_NAME).toString();
                    if (readMessage != null) {
                        //将接受的信息插入到数据库
                        ContentValues values2 = new ContentValues();
                        values2.put("name", mConnectedDeviceName);
                        values2.put("pdate", pdate);
                        values2.put("informations", readMessage);
                        DatabaseHelper insertdbHelper2 = new DatabaseHelper(ChatActivity.this, "zhsf_db");
                        SQLiteDatabase insertdb2 = insertdbHelper2.getWritableDatabase();
                        insertdb2.insert("info", null, values2);
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
                case Task.TASK_GET_REMOTE_STATE:
//                    setTitle((String) msg.obj);
                    mTvState.setText((String) msg.obj);
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

        // 打开蓝牙设备
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUES_BT_ENABLE_CODE);//使能设备
        } else {
            // 默认设备作为服务端
            startServiceAsServer();
        }
    }


    /**
     * 初始化view
     */
    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chat);
        mTvState = (TextView) findViewById(R.id.tv_state);
        mRootLayout = (LinearLayout) findViewById(R.id.root);
        mChatLayout = (LinearLayout) findViewById(R.id.topPanel);
        mList = (ListView) findViewById(R.id.lv_chat);//聊天的列表
        mInput = (EditText) findViewById(R.id.inputEdit);

        mSendBtn = (Button) findViewById(R.id.sendBtn);
        mEmoButton = (ImageView) findViewById(R.id.emotionBtn);
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
        mChatListAdapter = new ChatListViewAdapter(this, mChatContentData);//创建一个适配器

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
                imm.showSoftInput(mInput, 0);
                showEmoPanel(false);
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
        if (mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
        // 停止服务
        TaskService.stop(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendBtn:
                if (TaskService.state == TaskService.BT_STAT_ONLINE) {//并且如果是在线状态的话
                    String msg = mInput.getText().toString().trim();
                    TaskService.newTask(new Task(mHandler, Task.TASK_GET_REMOTE_STATE, null));//通过点击按钮触发相应线程的启动，比较巧妙，值得学习
                    if (msg.length() == 0) {
                        ToastUtil.show(ChatActivity.this, "聊天内容为空");
                        SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
                        return;
                    }
                    //------ DEUBG ------
                    TaskService.newTask(new Task(mHandler, Task.TASK_SEND_MSG, new Object[]{msg}));
                    showOwnMessage(msg);//立马显示自己发送的消息，所以在handler里面就没有再做处理
                    isHaspressed = true;//数据库可以开始记录消息啦
                    mInput.setText("");
                } else {
                    ToastUtil.show(ChatActivity.this, "没有连接其它用户，请先扫描并选择周围用户");
                    SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_ERR);
                    break;
                }
                break;
            case R.id.emotionBtn:
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
            mEmoView.setVisibility(View.VISIBLE);
            mEmoButton.setImageResource(R.drawable.emo_collapse);
            ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
            params.height = mChatLayout.getHeight() - mScrollHeight;
            mChatLayout.setLayoutParams(params);
            isShowEmo = true;
        } else if (!show && isShowEmo) {
            mEmoView.setVisibility(View.GONE);
            mEmoButton.setImageResource(R.drawable.emo_bkg);
            ViewGroup.LayoutParams params = mChatLayout.getLayoutParams();
            params.height = mChatLayout.getHeight() + mScrollHeight;
            mChatLayout.setLayoutParams(params);
            isShowEmo = false;
        }
        if (!isUpdate && show) {
            LayoutParams para = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mRootLayout.addView(mEmoView, para);
            isUpdate = true;
        }
    }


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
        SimpleDateFormat df1 = new SimpleDateFormat("E MM月dd日   HH:mm ");
        data.put(ChatListViewAdapter.KEY_DATE, df1.format(System.currentTimeMillis()).toString());
        data.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        mChatContentData.add(data);
        mChatListAdapter.notifyDataSetChanged();
        SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_RECV);
    }

    /**
     * 显示自己信息
     *
     * @param
     */
    private void showOwnMessage(String msg) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(ChatListViewAdapter.KEY_ROLE, ChatListViewAdapter.ROLE_OWN);//哪个角色的消息
        map.put(ChatListViewAdapter.KEY_NAME, mBluetoothAdapter.getName());
        map.put(ChatListViewAdapter.KEY_TEXT, msg);
        SimpleDateFormat df2 = new SimpleDateFormat("E MM月dd日  HH:mm ");
        map.put(ChatListViewAdapter.KEY_DATE, df2.format(System.currentTimeMillis()).toString());
        map.put(ChatListViewAdapter.KEY_SHOW_MSG, true);
        mChatContentData.add(map);
        mChatListAdapter.notifyDataSetChanged();
        SoundEffect.getInstance(ChatActivity.this).play(SoundEffect.SOUND_SEND);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUES_BT_ENABLE_CODE && resultCode == RESULT_OK) {//使能蓝牙
            startServiceAsServer();
        } else if (requestCode == REQUES_SELECT_BT_CODE && resultCode == RESULT_OK) {//选择设备进行连接
            mRemoteDevice = data.getParcelableExtra("DEVICE");
            if (mRemoteDevice == null)
                return;
            TaskService.newTask(new Task(mHandler, Task.TASK_START_CONN_THREAD, new Object[]{mRemoteDevice}));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


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

}


