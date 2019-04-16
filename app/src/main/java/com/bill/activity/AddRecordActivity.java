package com.bill.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.alibaba.fastjson.JSON;
import com.bill.R;
import com.bill.adapter.CategoryRecyclerAdapter;
import com.bill.bean.RecordBean;
import com.bill.util.GlobalUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddRecordActivity extends AppCompatActivity implements View.OnClickListener, CategoryRecyclerAdapter.OnCategoryClickListener {

    private static String TAG = "AddRecordActivity";

    private EditText editText;
    private TextView amountText;
    private String userInput = "";
    private RecyclerView recyclerView;
    private CategoryRecyclerAdapter adapter;
    private String category = "General";
    private int type = 1;
    private String remark = category;

    private RecordBean recordBean = new RecordBean();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    Bundle bundle = new Bundle();
                    String jsonStr = JSON.toJSONString(msg.obj);
                    bundle.putString("record", jsonStr);
                    Intent intent = AddRecordActivity.this.getIntent();
                    intent.putExtras(bundle);
                    AddRecordActivity.this.setResult(Activity.RESULT_OK, intent);
                    AddRecordActivity.this.finish();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        findViewById(R.id.keyboard_one).setOnClickListener(this);
        findViewById(R.id.keyboard_two).setOnClickListener(this);
        findViewById(R.id.keyboard_three).setOnClickListener(this);
        findViewById(R.id.keyboard_four).setOnClickListener(this);
        findViewById(R.id.keyboard_five).setOnClickListener(this);
        findViewById(R.id.keyboard_six).setOnClickListener(this);
        findViewById(R.id.keyboard_seven).setOnClickListener(this);
        findViewById(R.id.keyboard_eight).setOnClickListener(this);
        findViewById(R.id.keyboard_nine).setOnClickListener(this);
        findViewById(R.id.keyboard_zero).setOnClickListener(this);

        amountText = findViewById(R.id.textView_amount);
        editText = findViewById(R.id.editText);
        editText.setText(remark);

        handleDot();
        handleBackspace();
        handleTypeChange();
        handleDone();

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new CategoryRecyclerAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter.notifyDataSetChanged();

        adapter.setOnCategoryClickListener(this);
    }

    private void handleDot() {
        findViewById(R.id.keyboard_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!userInput.contains(".")) {
                    userInput += ".";
                }
            }
        });
    }

    private void handleBackspace() {
        findViewById(R.id.keyboard_backspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userInput.length() > 0) {
                    userInput = userInput.substring(0, userInput.length() - 1);
                }

                if (userInput.length() > 0 && userInput.charAt(userInput.length() - 1) == '.') {
                    userInput = userInput.substring(0, userInput.length() - 1);
                }

                updateAmountText();
            }
        });
    }

    private void handleTypeChange() {

        ImageView imageView = findViewById(R.id.keyboard_type);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1) {
                    type = 2;
                    imageView.setImageResource(R.drawable.baseline_remove_black_36);
                } else {
                    type = 1;
                    imageView.setImageResource(R.drawable.baseline_add_black_36);
                }

                adapter.changeType(type);
                category = adapter.getSelected();
            }
        });
    }

    private void handleDone() {
        findViewById(R.id.keyboard_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!userInput.equals("")) {
                    double amount = Double.valueOf(userInput);

                    RecordBean record = new RecordBean();

                    record.setAmount(amount);
                    record.setType(type);
                    record.setCategory(category);
                    record.setRemark(editText.getText().toString());
                    record.setUid(1);

                    String jsonStr = JSON.toJSONString(record);
                    //MediaType  设置Content-Type 标头中包含的媒体类型值
                    RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                            , jsonStr);

                    Request request = new Request.Builder()
                            .url(GlobalUtil.ip + "addRecord")//请求的url
                            .post(requestBody)
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
                            String res = response.body().string();
                            if (res.equals("添加成功")) {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = record;
                                mHandler.sendMessage(message);
                            }

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Amount is 0!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String input = button.getText().toString();
        Log.d(TAG, input);

        if (userInput.contains(".")) {

            if (userInput.split("\\.").length == 1 || userInput.split("\\.")[1].length() < 2) {
                userInput += input;
            }
        } else {
            userInput += input;
        }
        updateAmountText();
    }

    private void updateAmountText() {
        Log.d(TAG, "userinput" + userInput);

        if (userInput.contains(".")) {
            if (userInput.split("\\.").length == 1) {
                amountText.setText(userInput + "00");
            } else if (userInput.split("\\.")[1].length() == 1) {
                amountText.setText(userInput + "0");
            } else if (userInput.split("\\.")[1].length() == 2) {
                amountText.setText(userInput);
            }
        } else {
            if (userInput.equals("")) {
                amountText.setText("0.00");
            } else {
                amountText.setText(userInput + ".00");
            }

        }
    }

    @Override
    public void onClick(String category) {
        this.category = category;
        editText.setText(category);
    }
}