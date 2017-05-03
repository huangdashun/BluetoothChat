/**
 * @���� �ֱ��ģ����䣺ling20081005@126.com��
 * @���� http://blog.csdn.net/evankaka/
 * @��������������΢�������б��
 */
package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.utils.ToastUtil;

/**
 * Created by hs on 2017/3/27.
 */
public class PopDialogActivity extends Activity implements OnClickListener {
    private static final String TAG = "PopDialogActivity";
    private LinearLayout mDeviceVisible;
    private LinearLayout mAlterName;
    private LinearLayout mChattingRecords;
    private LinearLayout mExit;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pop_dialog);
        initView();
    }


    private void initView() {
        mDeviceVisible = (LinearLayout) findViewById(R.id.device_visible);//设备可见
        mAlterName = (LinearLayout) findViewById(R.id.alter_name);//修改昵称
        mChattingRecords = (LinearLayout) findViewById(R.id.chatting_records);//聊天记录
        mExit = (LinearLayout) findViewById(R.id.exit);
        // 获得蓝牙管理器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            Toast.makeText(this, "该设备没有蓝牙设备", Toast.LENGTH_LONG).show();
            return;
        }

        mDeviceVisible.setOnClickListener(this);
        mAlterName.setOnClickListener(this);
        mChattingRecords.setOnClickListener(this);
        mExit.setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.device_visible://设备可见
                ensureDiscoverable();
                break;
            case R.id.alter_name://修改昵称
                alterName();
                break;
            case R.id.chatting_records://聊天记录
                Intent recordIntent = new Intent(PopDialogActivity.this, RecordListActivity.class);
                startActivity(recordIntent);
                break;
            case R.id.exit://退出登录
                Intent loginActivity = new Intent(PopDialogActivity.this, LoginActivity.class);
                loginActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginActivity);
                sendBroadcast(new Intent("close.chat.activity"));
                finish();
        }
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

    /**
     * 修改昵称
     */
    private void alterName() {
        // 调用设置用户名方法
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText devNameEdit = new EditText(this);
        builder.setView(devNameEdit);
        builder.setTitle("请输入用户名");
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (devNameEdit.getText().toString().length() != 0)
                    mBluetoothAdapter.setName(devNameEdit.getText().toString());
                ToastUtil.show(getApplicationContext(), "修改用户名成功");
            }
        });
        builder.create();
        builder.show();
    }
}
