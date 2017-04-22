package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import huangshun.it.com.btproject.DB.DBAdapter;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.bean.User;
import huangshun.it.com.btproject.utils.ToastUtil;

/**
 * 找回密码
 */
public class ForgetPwdActivity extends Activity {

    private ImageView mBackLogin;
    private LinearLayout mLlPassword;
    private Button mRetrievePassword;
    private DBAdapter dbAdapter;
    private EditText mEtUsername;
    private TextView mTvPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_forget_pwd);

        dbAdapter = new DBAdapter(this);
        //打开数据库
        dbAdapter.open();
        initView();
        initListener();

    }


    private void initView() {
        mBackLogin = (ImageView) findViewById(R.id.back);//返回
        mRetrievePassword = (Button) findViewById(R.id.retrieve_password);//找回密码
        mTvPassword = (TextView) findViewById(R.id.password);//密码
        mLlPassword = (LinearLayout) findViewById(R.id.ll_password);
        mEtUsername = (EditText) findViewById(R.id.username);//输入的用户名
    }

    private void initListener() {
        mBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRetrievePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取用户输入的用户名
                final String username = mEtUsername.getText().toString();
                //通过用户输入的用户名查询数据库
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        User[] users = dbAdapter.queryUserData(username);
                        if (users != null) {
                            final String password = users[0].getPassword();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLlPassword.setVisibility(View.VISIBLE);
                                    mTvPassword.setText(password);
                                }
                            });
                        } else {
                            //弹出toast ,没有该用户
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.show(ForgetPwdActivity.this, "该用户名不存在");
                                    mLlPassword.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

}
