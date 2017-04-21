package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import huangshun.it.com.btproject.R;

/**
 * 找回密码
 */
public class ForgetPwdActivity extends Activity {

    private TextView mBackLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);
        mBackLogin = (TextView) findViewById(R.id.login_btn);
        mBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgetPwdActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
