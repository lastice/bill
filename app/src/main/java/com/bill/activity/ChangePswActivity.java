package com.bill.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class ChangePswActivity extends AppCompatActivity {

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    String res = (String) msg.obj;
                    if (res.equals("修改成功")) {
                        ChangePswActivity.this.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        EditText formerPassword = findViewById(R.id.former_password);
        EditText newPassword = findViewById(R.id.new_password);
        EditText checkPassword = findViewById(R.id.check_password);
        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("text", getBaseContext().MODE_PRIVATE);
                String password = sharedPreferences.getString("password", "");
                String name = sharedPreferences.getString("name", "");
                if (!formerPassword.getText().toString().equals(password)) {
                    Toast.makeText(getApplicationContext(), "原密码错误", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.getText().toString().equals(checkPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else if (newPassword.getText().toString().equals(formerPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "新密码不能与原密码相同", Toast.LENGTH_SHORT).show();
                } else {
                    //post方式提交的数据
                    FormBody formBody = new FormBody.Builder()
                            .add("name", name)
                            .add("password", newPassword.getText().toString())
                            .build();

                    Request request = new Request.Builder()
                            .url(GlobalUtil.ip + "updateUser")//请求的url
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
            }
        });


    }
}
