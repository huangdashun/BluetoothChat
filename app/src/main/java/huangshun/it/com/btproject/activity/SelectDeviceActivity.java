package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import huangshun.it.com.btproject.R;

public class SelectDeviceActivity extends Activity implements OnClickListener, OnItemClickListener {
    private final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private Button mScanBtn;
    private ListView mDevList;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> mDeviceNameList = new ArrayList<>();
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();
    private int requestBluCode = 1;//请求打开蓝牙的请求码
    private ImageView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scan_device);

        mDevList = (ListView) findViewById(R.id.devList);
        mBack = (ImageView) findViewById(R.id.im_back);
        mBack.setOnClickListener(this);

        mDevList.setOnItemClickListener(this);

        mScanBtn = (Button) findViewById(R.id.scanBtn);
        mScanBtn.setOnClickListener(this);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mDeviceNameList);

        mDevList.setAdapter(adapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "该设备不支持蓝牙设备", Toast.LENGTH_LONG).show();
            return;
        }

        // 设置未打开，请求打开设备
        if (!mBluetoothAdapter.isEnabled()) {
            // 请求打开蓝牙设备
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, requestBluCode);
        } else {
            findDevice();
        }


        // 注册广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
//		ChatActivity.sAliveCount++;
        super.onResume();
    }

    @Override
    protected void onPause() {
//		ChatActivity.sAliveCount--;
        super.onPause();
    }

    // 创建一个发现蓝牙设备的广播接收者
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 当发现设备时
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 获取蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 已包含该设备
                if (mDeviceList.contains(device)) {
                    return;
                }
                // 添加蓝牙设备信息
                mDeviceNameList.add(device.getName() + "\n" + device.getAddress());
                System.out.println(device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device);
                adapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // 取消扫描进度显示
//				setProgressBarIndeterminateVisibility(false);
            }
        }
    };

    /**
     * 开始扫描
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scanBtn) {
            if (!mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.startDiscovery();
//			// 开始显示进度
//			setProgressBarIndeterminateVisibility(true);
            }
        } else if (v.getId() == R.id.im_back) {
            finish();
        }

    }

    private void findDevice() {
        // 获得已经保存的配对设备(如果蓝牙未开启,那么会返回一个空集合)
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // 如果有配对的设备
        if (pairedDevices.size() > 0) {
            // 获取¬配对的设备
            for (BluetoothDevice device : pairedDevices) {
                mDeviceNameList.add(device.getName() + "\n" + device.getAddress());
                mDeviceList.add(device);
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestBluCode) {
            if (resultCode == RESULT_OK) {
                System.out.println("设备打开成功");
                findDevice();
            } else {
                System.out.println("设备打开失败");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        String targetDev = mDeviceNameList.get(arg2);
        System.out.println(targetDev);
        // 将点击的设备对象保存到Intent中，交给ChatActivity
        Intent data = new Intent();
        data.putExtra("DEVICE", mDeviceList.get(arg2));
        setResult(RESULT_OK, data);
        this.finish();
    }

}
