package huangshun.it.com.btproject.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import huangshun.it.com.btproject.Model.Task;
import huangshun.it.com.btproject.view.ChatListViewAdapter;
import huangshun.it.com.btproject.protocol.DataProtocol;
import huangshun.it.com.btproject.protocol.Message;
import huangshun.it.com.btproject.sound.SoundEffect;


/**
 * 任务处理服务
 *
 * @author Administrator
 */
public class TaskService extends Service {
    public static final int BT_STAT_WAIT = 0;//等待
    public static final int BT_STAT_CONN = 1;//连接
    public static final int BT_STAT_ONLINE = 2;//在线
    public static final int BT_STAT_UNKNOWN = 3;//未知
    public static int state = 3;

    private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String DEVICE_NAME = "device_name";

    private final String TAG = "TaskService";
    private TaskThread mThread;//启动任务服务线程

    private BluetoothAdapter mBluetoothAdapter;

    private AcceptThread mAcceptThread;//等待客户端连接线程
    private ConnectThread mConnectThread;//维持连接的线程
    private ConnectedThread mConnThread;//作为客户端连接指定的蓝牙设备线程

    private boolean isServerMode = true;

    private static Handler mActivityHandler; //负责更新UI的handler

    // 任务队列
    private static ArrayList<Task> mTaskList = new ArrayList<Task>();

    private Handler mServiceHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Task.TASK_GET_REMOTE_STATE:
                    if (mActivityHandler != null) {
                        android.os.Message activityMsg = mActivityHandler.obtainMessage();
                        activityMsg.what = msg.what;
                        if (mAcceptThread != null && mAcceptThread.isAlive()) {
                            activityMsg.obj = "等待连接...";
                            activityMsg.arg1 = BT_STAT_WAIT;
                        } else if (mConnThread != null && mConnThread.isAlive()) {
                            activityMsg.obj = mConnThread.getRemoteName() + "[在线]";
                            activityMsg.arg1 = BT_STAT_ONLINE;
                        } else if (mConnectThread != null && mConnectThread.isAlive()) {
                            SoundEffect.getInstance(TaskService.this).play(3);
                            activityMsg.obj = "正在连接："
                                    + mConnectThread.getDevice().getName();
                            activityMsg.arg1 = BT_STAT_CONN;

                        } else {
                            activityMsg.obj = "对方已经下线";
                            activityMsg.arg1 = BT_STAT_UNKNOWN;
                            SoundEffect.getInstance(TaskService.this).play(2);
                            // 重新等待连接
                            mAcceptThread = new AcceptThread();
                            mAcceptThread.start();
                            isServerMode = true;

                        }
                        state = activityMsg.arg1;
                        mActivityHandler.sendMessage(activityMsg);
                    } else {
                        return;
                    }

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Your device is not support Bluetooth!");
            return;
        }
        mThread = new TaskThread();
        mThread.start();
    }


    public static void start(Context context, Handler handler) {
        mActivityHandler = handler;
        Intent intent = new Intent(context, TaskService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, TaskService.class);
        context.stopService(intent);
    }


    public static void newTask(Task target) {
        synchronized (mTaskList) {
            //将任务添加到任务队列中
            mTaskList.add(target);
        }
    }

    private class TaskThread extends Thread {
        private boolean isRun = true;//判断线程是否还在运行
        private int mCount = 0;//用来判断线程是否还在运行

        //被调用时.将标记设置为false,表示线程停止运行
        public void cancel() {
            isRun = false;
        }

        @Override
        public void run() {
            Task task;
            while (isRun) {//如果为true

                // 有任务
                if (mTaskList.size() > 0) {
                    synchronized (mTaskList) {
                        // 获得任务
                        task = mTaskList.get(0);
                        doTask(task);
                    }
                } else {
                    try {
                        Thread.sleep(200);
                        mCount++;
                    } catch (InterruptedException e) {
                    }
                    // 每过10秒钟进行一次状态检查
                    if (mCount >= 50) {
                        mCount = 0;
                        // 检查远程设备状态
                        android.os.Message handlerMsg = mServiceHandler
                                .obtainMessage();
                        handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
                        mServiceHandler.sendMessage(handlerMsg);
                    }
                }
            }
        }

    }

    //对应三个线程，其中mConnThread是在mConnectThread的run()方法中new出来的
    private void doTask(Task task) {
        switch (task.getTaskID()) {
            case Task.TASK_START_ACCEPT://作为服务器接收等待客户端连接的线程
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                isServerMode = true;
                break;
            case Task.TASK_START_CONN_THREAD://作为客户端去连接远程服务器的线程
                if (task.mParams == null || task.mParams.length == 0) {
                    break;
                }
                BluetoothDevice remote = (BluetoothDevice) task.mParams[0];
                mConnectThread = new ConnectThread(remote);
                mConnectThread.start();
                isServerMode = false;
                break;
            case Task.TASK_SEND_MSG:
                boolean success = false;
                if (mConnThread == null || !mConnThread.isAlive()
                        || task.mParams == null || task.mParams.length == 0) {
                    Log.e(TAG, "mConnThread or task.mParams null");
                } else {
                    byte[] msg = null;
                    try {

                        msg = DataProtocol.packMsg((String) task.mParams[0]);
                        success = mConnThread.write(msg);

                    } catch (UnsupportedEncodingException e) {
                        success = false;
                    }
                }
                if (!success) {
                    android.os.Message returnMsg = mActivityHandler.obtainMessage();
                    returnMsg.what = Task.TASK_SEND_MSG_FAIL;
                    returnMsg.obj = "消息发送失败";
                    mActivityHandler.sendMessage(returnMsg);
                }
                break;
        }

        // 移除任务
        mTaskList.remove(task);//每次保证任务列表里面只有一个任务，task = mTaskList.get(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThread.cancel();
    }


    /**
     * 等待客户端连接线程
     *
     * @author Administrator
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private boolean isCancel = false;

        public AcceptThread() {
            Log.d(TAG, "AcceptThread");
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        "MT_Chat_Room", UUID.fromString(UUID_STR));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    // 阻塞等待
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
                    }
                } catch (IOException e) {//防止异常结束
                    if (!isCancel) {
                        try {
                            mmServerSocket.close();
                        } catch (IOException e1) {
                        }
                        //异常结束时,再次监听
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                        isServerMode = true;
                    }
                    break;
                }
                if (socket != null) {
                    //管理已经连接的客户端
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                    }
                    mAcceptThread = null;
                    break;
                }
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "AcceptThread canceled");
                isCancel = true;
                isServerMode = false;
                mmServerSocket.close();
                mAcceptThread = null;
                if (mConnThread != null && mConnThread.isAlive()) {
                    mConnThread.cancel();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 作为客户端连接指定的蓝牙设备线程
     *
     * @author Administrator
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {

            Log.d(TAG, "ConnectThread");

            if (mAcceptThread != null && mAcceptThread.isAlive()) {
                mAcceptThread.cancel();
            }

            if (mConnThread != null && mConnThread.isAlive()) {
                mConnThread.cancel();
            }

            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString(UUID_STR));
            } catch (IOException e) {
                Log.d(TAG, "createRfcommSocketToServiceRecord error!");
            }

            mmSocket = tmp;
        }

        public BluetoothDevice getDevice() {
            return mmDevice;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e(TAG, "Connect server failed");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
                isServerMode = true;
                return;
            } // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
            mConnectThread = null;
        }
    }

    /**
     * 管理已经连接的客户端
     *
     * @param socket
     */
    private void manageConnectedSocket(BluetoothSocket socket) {
        // 启动子线程来维持连接
        mConnThread = new ConnectedThread(socket);
        mConnThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutStream;
        private BufferedOutputStream mBos;
        private byte[] buffer;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread");
            mSocket = socket;
            try {
                mInputStream = socket.getInputStream();
                mOutStream = socket.getOutputStream();
            } catch (IOException e) {
            }
            mBos = new BufferedOutputStream(mOutStream);
        }

        public OutputStream getOutputStream() {
            return mOutStream;
        }

        public boolean write(byte[] msg) {
            if (msg == null)
                return false;
            try {
                mBos.write(msg);
                mBos.flush();

                mActivityHandler.obtainMessage(Task.TASK_SEND_MSG, -1, -1, new String(msg)).sendToTarget();
                System.out.println("Write:" + msg);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        public String getRemoteName() {
            return mSocket.getRemoteDevice().getName();
        }


        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
            mConnThread = null;
        }

        public void run() {
            try {
                write(DataProtocol.packMsg(mBluetoothAdapter.getName()
                        + "已经上线\n"));//获取本地蓝牙适配器的蓝牙名称，这一条消息默认发送出去啦，
                //但消息记录里面不应该有这条消息，消息记录里面记录按过发送键的消息
            } catch (UnsupportedEncodingException e2) {
            }
            int size;
            Message msg;
            android.os.Message handlerMsg;
            buffer = new byte[1024];

            BufferedInputStream bis = new BufferedInputStream(mInputStream);
            // BufferedReader br = new BufferedReader(new
            // InputStreamReader(mmInStream));
            HashMap<String, Object> data;
            while (true) {
                try {
                    size = bis.read(buffer);
                    msg = DataProtocol.unpackData(buffer);
                    if (msg == null)
                        continue;

                    if (mActivityHandler == null) {
                        return;
                    }

                    msg.remoteDevName = mSocket.getRemoteDevice().getName();//得到对方设备的名字
                    if (msg.type == DataProtocol.TYPE_FILE) {
                        // 文件接收处理忽略

                    } else if (msg.type == DataProtocol.TYPE_MSG) {
                        data = new HashMap<String, Object>();
                        System.out.println("Read data.");
                        data.put(ChatListViewAdapter.KEY_ROLE,
                                ChatListViewAdapter.ROLE_TARGET);
                        data.put(ChatListViewAdapter.KEY_NAME,
                                msg.remoteDevName);
                        data.put(ChatListViewAdapter.KEY_TEXT, msg.msg);

                        // 通过Activity更新到UI上
                        handlerMsg = mActivityHandler.obtainMessage();
                        handlerMsg.what = Task.TASK_RECV_MSG;
                        handlerMsg.obj = data;
                        mActivityHandler.sendMessage(handlerMsg);
                    }
                } catch (IOException e) {
                    try {
                        mSocket.close();
                    } catch (IOException e1) {
                    }
                    mConnThread = null;
                    if (isServerMode) {
                        // 检查远程设备状态
                        handlerMsg = mServiceHandler.obtainMessage();
                        handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
                        mServiceHandler.sendMessage(handlerMsg);
                        SoundEffect.getInstance(TaskService.this).play(2);
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                    }
                    break;
                }
            }
        }
    }

    // ================================================================

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
