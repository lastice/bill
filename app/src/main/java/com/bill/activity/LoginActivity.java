package com.bill.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.bill.R;
import com.bill.util.GlobalUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";

    private EditText usn;
    private EditText psw;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    JSONArray jsonArray = JSONArray.parseArray((String) msg.obj);
                    Log.d(TAG, jsonArray.toString());
                    String username = jsonArray.getJSONObject(0).getString("name");
                    int uid = jsonArray.getJSONObject(0).getInteger("uid");
                    String email = jsonArray.getJSONObject(0).getString("email");
                    String password = jsonArray.getJSONObject(0).getString("password");
                    if (username == null || username == "") {
                        Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
                    } else {
                        saveToPre(getBaseContext(), username, password);
                        Bundle bundle = new Bundle();
                        bundle.putInt("uid", uid);
                        bundle.putString("name", username);
                        bundle.putString("email", email);
                        Intent intent = LoginActivity.this.getIntent();
                        intent.putExtras(bundle);
                        LoginActivity.this.setResult(Activity.RESULT_OK, intent);
                        LoginActivity.this.finish();
                    }
                }
            }
        }
    };

    public static void readFromPre(Context context, EditText username, EditText psw) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("text", context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String password = sharedPreferences.getString("password", "");
        username.setText(name);
        psw.setText(password);
    }

    public static void saveToPre(Context context, String name, String password) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("text", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("password", password);
        editor.commit();
    }

    public static void clearPre(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("text", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usn = findViewById(R.id.username);
        psw = findViewById(R.id.password);

        //从缓存中读取账号密码
        readFromPre(this, usn, psw);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //post方式提交的数据
                FormBody formBody = new FormBody.Builder()
                        .add("account", usn.getText().toString())
                        .add("password", psw.getText().toString())
                        .build();

                Request request = new Request.Builder()
                        .url(GlobalUtil.ip + "login")//请求的url
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
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 3);
            }
        });

        findViewById(R.id.forget_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPswActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent date) {
        super.onActivityResult(requestCode, resultCode, date);

        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = date.getExtras();

            usn.setText(bundle.getString("name"));
            psw.setText(bundle.getString("password"));
        }
    }
}