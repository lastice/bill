package com.bill;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bill.activity.MainActivity;
import com.bill.adapter.ListViewAdapter;
import com.bill.bean.RecordBean;
import com.bill.util.GlobalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@SuppressLint("ValidFragment")
public class MainFragment extends Fragment implements AdapterView.OnItemLongClickListener {

    private String TAG = "MainFragment";

    private View rootView;//寻找dayView和listView
    private TextView dayView;
    private ListView listView;
    private ListViewAdapter listViewAdapter;

    private List<RecordBean> records = new ArrayList<>();

    private String date;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    String uuid = (String) msg.obj;
                    int i = 0;
                    for (RecordBean recordBean : records) {
                        if (recordBean.getUuid().equals(uuid)) {
                            break;
                        }
                        i++;
                    }
                    records.remove(i);
                    listViewAdapter.setData(records);
                    listViewAdapter.notifyDataSetChanged();
                    listView.setAdapter(listViewAdapter);
                    MainActivity.reloadTickerView();
                    break;
                }
            }
        }
    };

    public static MainFragment newInstance(String date) {
//        this.date = date;
//        Log.d(TAG,this.date);
//        records = GlobalUtil.getInstance().databaseHelper.readRecords(date);
        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("date", date);
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    public String getDate() {
        return this.date;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //处理传入的数据
        this.date = getArguments().getString("date");
        this.records = GlobalUtil.getInstance().databaseHelper.readRecords(date);
        Log.d(TAG, this.date);
        initView();
    }

    //添加或者删除记录调用
    public void reload() {
        this.date = getArguments().getString("date");
        Log.d(TAG, "233" + date);
        this.records = GlobalUtil.getInstance().databaseHelper.readRecords(this.date);
        listViewAdapter.setData(this.records);
        if (listViewAdapter.getCount() > 0) {
            rootView.findViewById(R.id.no_record_layout).setVisibility(View.INVISIBLE);
        }
    }


    private void initView() {

        dayView = rootView.findViewById(R.id.day_text);
        listView = rootView.findViewById(R.id.listView);
        dayView.setText(this.date);

        listViewAdapter = new ListViewAdapter(getContext());
        listViewAdapter.setData(records);
        //listViewAdapter.notifyDataSetChanged();
        listView.setAdapter(listViewAdapter);

        if (listViewAdapter.getCount() > 0) {
            rootView.findViewById(R.id.no_record_layout).setVisibility(View.INVISIBLE);
        }
        listView.setOnItemLongClickListener(this);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_IDLE: {
                        MainActivity.fab.show();
                        break;
                    }
                    default: {
                        MainActivity.fab.hide();
                        break;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    public LinkedList<Double> getTotalCost() {
        LinkedList<Double> totalCost = new LinkedList<>();

        double outcome = 0;
        double income = 0;

        for (RecordBean record : records) {
            if (record.getType() == 1) {
                outcome += record.getAmount();
            } else {
                income += record.getAmount();
            }
        }
        totalCost.add(outcome);
        totalCost.add(income);

        return totalCost;
    }


    //长按
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showDialog(position);
        return false;
    }

    private void showDialog(int index) {
        String[] options = {"删除"};

        RecordBean selectedRecord = records.get(index);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.create();
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    String uuid = selectedRecord.getUuid();

                    //post方式提交的数据
                    FormBody formBody = new FormBody.Builder()
                            .add("uuid", uuid)
                            .build();

                    Request request = new Request.Builder()
                            .url(GlobalUtil.ip + "deleteRecord")//请求的url
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

                            if (res.equals("删除成功")) {
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = uuid;
                                mHandler.sendMessage(msg);
                            }
                        }
                    });
                }

            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

}