package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import huangshun.it.com.btproject.DB.DBAdapter;
import huangshun.it.com.btproject.DB.DBAdapterImage;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.bean.User;
import huangshun.it.com.btproject.databinding.ActivityLoginBinding;
import huangshun.it.com.btproject.utils.ToastUtil;
import huangshun.it.com.btproject.view.SelectPicPopupWindow;


/**
 * 登录页面
 */

public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private Context mContext;
    private SelectPicPopupWindow mMenuWindow; // 自定义的头像编辑弹出框
    private static final String IMAGE_FILE_NAME = "avatarImage.jpg";// 头像文件名称
    private String mUrlpath;            // 图片本地路径
    private static final int REQUESTCODE_PICK = 0;        // 相册选图标记
    private static final int REQUESTCODE_TAKE = 1;        // 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 2;    // 图片裁切标记
    private static final int REQUESTCODE_REGISTER = 3;    // 注册账号
    private ActivityLoginBinding mBinding;
    private String userInfo = "saveUserNamePwd";//存放账号密码的key
    private SharedPreferences sp;
    private DBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mContext = LoginActivity.this;


        dbAdapter = new DBAdapter(this);
        //打开数据库
        dbAdapter.open();
        initViews();
        initListener();
    }


    /**
     * 初始化页面控件
     */
    private void initViews() {
        sp = getSharedPreferences(userInfo, MODE_PRIVATE);
        boolean isRemember = sp.getBoolean("isRemember", false);//是否记录密码
        mBinding.rememberCheck.setChecked(isRemember);
        if (isRemember) {
            mBinding.etUsername.setText(sp.getString("username", ""));//设置用户名
            mBinding.etPassword.setText(sp.getString("password", ""));//设置密码
        } else {
            mBinding.etUsername.setText(sp.getString("username", ""));//设置用户名
        }
        initAvatar();//从数据库里初始化头像

    }


    private void initListener() {
        mBinding.avatarImg.setOnClickListener(this);// 头像图片
        mBinding.btnLogin.setOnClickListener(this);// 页面的登录按钮
        mBinding.forgetAccount.setOnClickListener(this);//忘记密码按钮
        mBinding.registerAccount.setOnClickListener(this);//注册账号
    }

    /**
     * 初始化头像
     */
    private void initAvatar() {
        DBAdapterImage db = new DBAdapterImage(this);
        //打开数据库
        db.open();
        Cursor cursor = db.queryAllData();
        if (cursor != null) {
            byte[] blob = cursor.getBlob(cursor.getColumnIndex("image"));
            //第二步，调用BitmapFactory的解码方法decodeByteArray把字节数组转换为Bitmap对象
            Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            //第三步，调用BitmapDrawable构造函数生成一个BitmapDrawable对象，该对象继承Drawable对象，所以在需要处直接使用该对象即可
            @SuppressWarnings("deprecation")
            BitmapDrawable bd = new BitmapDrawable(bmp);
            mBinding.avatarImg.setImageDrawable(bd);
        }
        db.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatarImg:// 更换头像点击事件
                mMenuWindow = new SelectPicPopupWindow(mContext, itemsOnClick);
                mMenuWindow.showAtLocation(findViewById(R.id.mainLayout),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case R.id.btn_login://登录按钮跳转事件
                if (dbAdapter.queryUserData(mBinding.etUsername.getText().toString()) != null) {

                    User[] user = dbAdapter.queryUserData(mBinding.etUsername.getText().toString());
                    if (user[0].password.equals(mBinding.etPassword.getText().toString())) {
                        ToastUtil.show(LoginActivity.this, "登录成功");
                        String username = mBinding.etUsername.getText().toString();
                        String password = mBinding.etPassword.getText().toString();
                        remember(username, password);//记住密码的话存入sp
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, ChatActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        ToastUtil.show(LoginActivity.this, "用户名与密码不匹配");
                    }

                } else {
                    ToastUtil.show(LoginActivity.this, "用户名不存在");
                }
                break;
            case R.id.forget_account://忘记密码
                startActivity(new Intent(mContext, ForgetPwdActivity.class));
                break;
            case R.id.register_account://注册账号
                startActivityForResult(new Intent(mContext, RegisterActivity.class), REQUESTCODE_REGISTER);
            default:
                break;
        }
    }

    /**
     * 记住账号和密码
     */
    private void remember(String username, String password) {
        if (mBinding.rememberCheck.isChecked()) {//检测用户名密码
            if (sp == null) {
                sp = getSharedPreferences(userInfo, MODE_PRIVATE);
            }
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("username", username);
            edit.putString("password", password);
            edit.putBoolean("isRemember", true);
            edit.commit();
        } else if (!mBinding.rememberCheck.isChecked()) {
            if (sp == null) {
                sp = getSharedPreferences(userInfo, MODE_PRIVATE);
            }
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("username", username);
            edit.putBoolean("isRemember", false);
            edit.commit();
        }
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMenuWindow.dismiss();
            switch (v.getId()) {
                // 拍照
                case R.id.takePhotoBtn:
                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //下面这句指定调用相机拍照后的照片存储的路径
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                                    IMAGE_FILE_NAME)));
                    startActivityForResult(takeIntent, REQUESTCODE_TAKE);
                    break;
                // 相册选择图片
                case R.id.pickPhotoBtn:
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                    // 如果朋友们要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                    pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*");
                    startActivityForResult(pickIntent, REQUESTCODE_PICK);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUESTCODE_PICK:// 直接从相册获取
                try {
                    startPhotoZoom(data.getData());
                } catch (NullPointerException e) {
                    e.printStackTrace();// 用户点击取消操作
                }
                break;
            case REQUESTCODE_TAKE:// 调用相机拍照
                File temp = new File(Environment.getExternalStorageDirectory() + "/" +
                        IMAGE_FILE_NAME);
                startPhotoZoom(Uri.fromFile(temp));
                break;
            case REQUESTCODE_CUTTING:// 取得裁剪后的图片
                if (data != null) {
                    setPicToView(data);
                }
                break;
            case REQUESTCODE_REGISTER://取得注册后的账号和密码
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    User user = (User) bundle.getSerializable("user");
                    mBinding.etUsername.setText(user.username);
                    mBinding.etPassword.setText(user.password);
                    Log.i(TAG, "执行了:" + user.toString());
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param picData
     */
    private void setPicToView(Intent picData) {
        Bundle extras = picData.getExtras();
        if (extras != null) {

            //第一步，将Drawable对象转化为Bitmap对象
            Bitmap bmp = extras.getParcelable("data");
            //第二步，声明并创建一个输出字节流对象
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //第三步，调用compress将Bitmap对象压缩为PNG格式，第二个参数为PNG图片质量，第三个参数为接收容器，即输出字节流os
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            //第四步，将输出字节流转换为字节数组，并直接进行存储数据库操作，注意，所对应的列的数据类型应该是BLOB类型
            ContentValues values = new ContentValues();
            values.put("image", os.toByteArray());
            DBAdapterImage db = new DBAdapterImage(this);
            //打开数据库
            db.open();
            Cursor ss = db.queryAllData();
            if (ss != null) {
                db.deleteAllData();
            }
            db.insert("imageviewinfo", values);
            //第一步，从数据库中读取出相应数据，并保存在字节数组中
            Cursor cursor = db.queryAllData();
            byte[] blob = cursor.getBlob(cursor.getColumnIndex("image"));
            //第二步，调用BitmapFactory的解码方法decodeByteArray把字节数组转换为Bitmap对象
            Bitmap bmp2 = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            //第三步，调用BitmapDrawable构造函数生成一个BitmapDrawable对象，该对象继承Drawable对象，所以在需要处直接使用该对象即可
            @SuppressWarnings("deprecation")
            BitmapDrawable bd = new BitmapDrawable(bmp2);
            mBinding.avatarImg.setImageDrawable(bd);
            db.close();
        }
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
}
