package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import huangshun.it.com.btproject.DB.DBAdapter;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.bean.User;
import huangshun.it.com.btproject.utils.ToastUtil;

/**
 * 注册账号
 */
public class RegisterActivity extends Activity {
    private DBAdapter mDBAdapter;
    private EditText mUsername, mPassword, mRepassword;
    private Button mBtnLogin, mBtnRegister;
    private ImageView mBack;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        mDBAdapter = new DBAdapter(this);
        //打开数据库
        mDBAdapter.open();
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mRepassword = (EditText) findViewById(R.id.repeat_word);
        mBack = (ImageView) findViewById(R.id.back);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {


                User user = new User();
                user.username = mUsername.getText().toString();
                user.password = mPassword.getText().toString();
                String repeatPwd = mRepassword.getText().toString();
                if (mDBAdapter.queryUserData(mUsername.getText().toString()) == null) {

                    if (mUsername.getText().toString().length() < 4 || mUsername.getText().toString().length() > 16) {
                        ToastUtil.show(RegisterActivity.this, "用户名应控制在：4-16位之间");
                        return;

                    }
                    if (mPassword.getText().toString().length() < 6 || mPassword.getText().toString().length() > 16) {
                        ToastUtil.show(RegisterActivity.this, "密码应控制在：6-16位之间");
                        return;
                    }
                    if (!mPassword.getText().toString().equalsIgnoreCase(repeatPwd)) {
                        ToastUtil.show(RegisterActivity.this, "重复密码不一致");
                        return;
                    }
                    long count = mDBAdapter.insert(user);
                    if (count == -1) {
                        ToastUtil.show(RegisterActivity.this, "注册失败");
                    } else {
                        ToastUtil.show(RegisterActivity.this, "注册成功");
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", user);
                        intent.putExtras(bundle);
                        setResult(3, intent);
                        finish();
                    }
                } else {
                    ToastUtil.show(RegisterActivity.this, "用户名已存在！");
                }

            }


        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
