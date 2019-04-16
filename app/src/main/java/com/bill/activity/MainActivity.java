package com.bill.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bill.R;
import com.bill.adapter.MainViewPagerAdapter;
import com.bill.bean.RecordBean;
import com.bill.util.ExcelUtil;
import com.bill.util.GlobalUtil;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "MainActivity";
    public static FloatingActionButton fab;
    private static ViewPager viewPager;
    private static MainViewPagerAdapter pagerAdapter;
    private static TickerView outcomeText;
    private static TickerView incomeText;
    private TextView usn;
    private TextView email;
    private String username;
    private int uid;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    JSONArray jsonArray = new JSONArray();
                    if ((String) msg.obj == null || (String) msg.obj == "") ;
                    else {
                        jsonArray = JSONArray.parseArray((String) msg.obj);
                    }

                    for (int i = 0; i < jsonArray.size(); i++) {
                        RecordBean recordBean = new RecordBean();
                        recordBean.setUid(jsonArray.getJSONObject(i).getInteger("uid"));
                        recordBean.setAmount(jsonArray.getJSONObject(i).getDouble("amount"));
                        recordBean.setCategory(jsonArray.getJSONObject(i).getString("category"));
                        recordBean.setDate(jsonArray.getJSONObject(i).getString("date"));
                        recordBean.setRemark(jsonArray.getJSONObject(i).getString("remark"));
                        recordBean.setType(jsonArray.getJSONObject(i).getInteger("type"));
                        recordBean.setUuid(jsonArray.getJSONObject(i).getString("uuid"));
                        recordBean.setTimeStamp(jsonArray.getJSONObject(i).getLong("time"));
                        GlobalUtil.records.add(recordBean);
                    }

                    pagerAdapter.reInitFragments();
                    pagerAdapter.notifyDataSetChanged();
                    viewPager.setOnPageChangeListener(MainActivity.this);
                    viewPager.setCurrentItem(pagerAdapter.getLastIndex());
                    reloadTickerView();
                    break;
                }
                default:
                    break;
            }

        }
    };

    public static void reloadTickerView() {
        LinkedList<Double> totalCost = pagerAdapter.getTotalCost(viewPager.getCurrentItem());

        String outcome = String.valueOf(totalCost.get(0));
        String income = String.valueOf(totalCost.get(1));

        outcomeText.setText(outcome);
        incomeText.setText(income);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalUtil.isLogin) {
                    Intent intent = new Intent(MainActivity.this, AddRecordActivity.class);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(getApplicationContext(), "请先登陆", Toast.LENGTH_SHORT).show();
                }

            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        View headView = navigationView.getHeaderView(0);

        usn = headView.findViewById(R.id.username);
        email = headView.findViewById(R.id.email);

        headView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalUtil.isLogin) {
                    Toast.makeText(getApplicationContext(), "您已登陆", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtras(new Bundle());
                    startActivityForResult(intent, 2);
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setElevation(0);
        GlobalUtil.getInstance().setContext(getApplicationContext());

        outcomeText = findViewById(R.id.outcome_text);
        outcomeText.setCharacterLists(TickerUtils.provideNumberList());
        incomeText = findViewById(R.id.income_text);
        incomeText.setCharacterLists(TickerUtils.provideNumberList());

        viewPager = findViewById(R.id.view_pager);//初始化viewPager
        pagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());//初始化
        //pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(pagerAdapter.getLastIndex());

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent date) {
        super.onActivityResult(requestCode, resultCode, date);

        //登陆后刷新侧划页面数据
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = date.getExtras();
            username = bundle.getString("name");
            uid = bundle.getInt("uid");
            usn.setText(usn.getText().toString() + username);
            email.setText(email.getText().toString() + bundle.getString("email"));
            GlobalUtil.isLogin = true;
            getRecord(uid);
        }

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = date.getExtras();
            GlobalUtil.records.add(JSON.parseObject(bundle.getString("record"), RecordBean.class));
            //reloadTickerView();

            pagerAdapter.notifyDataSetChanged();
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(pagerAdapter.getLastIndex());
            reloadTickerView();
        }

    }

    public void getRecord(int uid) {
        //post方式提交的数据
        FormBody formBody = new FormBody.Builder()
                .add("uid", uid + "")
                .build();

        Request request = new Request.Builder()
                .url(GlobalUtil.ip + "getRecord")//请求的url
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //注销对话框
    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage("确认注销账户");
        alertDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GlobalUtil.isLogin = false;
                LoginActivity.clearPre(getBaseContext());
                usn.setText("用户名 ：");
                email.setText("邮箱 ：");
                GlobalUtil.records.clear();
                pagerAdapter.reInitFragments();
                pagerAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(pagerAdapter.getLastIndex());
                Toast.makeText(getApplicationContext(), "注销成功", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chart) {
            if (GlobalUtil.isLogin) {
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivityForResult(intent, 3);
            } else {
                Toast.makeText(getApplicationContext(), "请先登陆", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_changePsw) {
            if (GlobalUtil.isLogin) {
                Intent intent = new Intent(MainActivity.this, ChangePswActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "请先登陆", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_export) {
            if (GlobalUtil.isLogin) {
                String filePath = "/sdcard/bill_" + username + ".xls";
                File file = new File(filePath);
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String[] title = {"类别", "类型", "时间", "金额", "备注"};
                ExcelUtil.initExcel(filePath, title);
                ExcelUtil.writeRecordListToExcel(GlobalUtil.records, filePath, getApplication());
            } else {
                Toast.makeText(getApplicationContext(), "请先登陆", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_logout) {
            if (GlobalUtil.isLogin) {
                //弹出对话框
                showAlertDialog();
            } else {
                Toast.makeText(getApplicationContext(), "请先登陆", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        reloadTickerView();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}