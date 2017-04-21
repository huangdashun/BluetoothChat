package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import huangshun.it.com.btproject.R;

/**
 * 闪屏页,通常是展示广告或appLogo的页面
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        //使图片全屏
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //在主线程中执行,跳转到主页
                startMainActivity();
            }


        }, 2000);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        //关闭当前页
        finish();
    }
}
