package com.bill.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bill.R;
import com.bill.util.GlobalUtil;
import com.bill.util.RSAUtil;

import java.io.IOException;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "RegisterActivity";

    private EditText usn;
    private EditText email;
    private EditText psw1;
    private EditText psw2;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    String res = (String) msg.obj;
                    if (res.equals("注册成功")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("name", usn.getText().toString());
                        bundle.putString("password", psw1.getText().toString());
                        Intent intent = RegisterActivity.this.getIntent();
                        intent.putExtras(bundle);
                        RegisterActivity.this.setResult(Activity.RESULT_OK, intent);
                        Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
                        RegisterActivity.this.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_register);
        usn = findViewById(R.id.register_username);
        psw1 = findViewById(R.id.register_password1);
        psw2 = findViewById(R.id.register_password2);
        email = findViewById(R.id.register_e_mail);


        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //有效邮箱正则表达式
                String pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
                boolean isMatch = Pattern.matches(pattern, email.getText().toString());
                //首先判断邮箱格式
                if (isMatch) {
                    if (!psw1.getText().toString().equals(psw2.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    } else {
                        //post方式提交的数据
                        PublicKey publicKey = RSAUtil.keyStrToPublicKey(RSAUtil.PUBLIC_KEY_STR);
                        String encryptedPassword = RSAUtil.encryptDataByPublicKey(psw1.getText().toString().getBytes(), publicKey);
                        Log.d(TAG, encryptedPassword.length() + "");
                        FormBody formBody = new FormBody.Builder()
                                .add("name", usn.getText().toString())
                                .add("password", encryptedPassword)
                                .add("email", email.getText().toString())
                                .build();

                        Request request = new Request.Builder()
                                .url(GlobalUtil.ip + "register")//请求的url
                                .post(formBody)
                                .build();

                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .writeTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(20, TimeUnit.SECONDS)
                                .build();
                        //创建/Call
                        Call call = okHttpClient.newCall(request);
                        //加入队列 异步操作
                        call.enqueue(new Callback() {
                            //请求错误回调方法
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("response", "连接失败");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String res = response.body().string();//返回的JSON字符串
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = res;
                                mHandler.sendMessage(msg);
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "邮箱格式不正确！", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}