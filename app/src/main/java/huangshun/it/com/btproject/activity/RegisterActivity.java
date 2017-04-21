package huangshun.it.com.btproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import huangshun.it.com.btproject.DB.DBAdapter;
import huangshun.it.com.btproject.R;
import huangshun.it.com.btproject.bean.User;

/**
 * 注册账号
 */
public class RegisterActivity extends Activity {
    private DBAdapter dbAdapter;
    private EditText username,password,repassword;
    private Button loginbtn,registerbtn;
    private Toast mToast;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbAdapter = new DBAdapter(this);
        //打开数据库
        dbAdapter.open();
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        loginbtn=(Button)findViewById(R.id.loginbtn);
        registerbtn=(Button)findViewById(R.id.registertbn);
        repassword=(EditText)findViewById(R.id.repassword);

        registerbtn.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){


                User user = new User();
                user.username = username.getText().toString();
                user.password = password.getText().toString();
                String repwd = repassword.getText().toString();
                if(dbAdapter.queryuserData(username.getText().toString())==null){

                    if(username.getText().toString().length()<4||username.getText().toString().length()>16){
                        showToast("用户名应控制在：4-16位之间");
                        return;

                    }
                    if(password.getText().toString().length()<6||password.getText().toString().length()>16){
                        showToast("密码应控制在：6-16位之间");
                        return;
                    }
                    if(!password.getText().toString().equalsIgnoreCase(repwd)){
                        showToast("重复密码不一致");
                        return;
                    }
                    long count = dbAdapter.insert(user);
                    if(count == -1 )
                    {
                        showToast("注册失败");
                    }
                    else
                    {
                        showToast("注册成功");
                        Intent intent = new Intent();
                        intent.setClass(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
                }else{
                    showToast("用户名已存在！");
                }

            }


        });

        loginbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void showToast(String msg){
        if(mToast == null){
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        }else{
            mToast.setText(msg);
        }
        mToast.show();
    }
}
