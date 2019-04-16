package com.bill.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bill.R;
import com.bill.bean.RecordBean;
import com.bill.util.DateUtil;
import com.bill.util.GlobalUtil;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {

    private List<RecordBean> records = new ArrayList<>();

    private LayoutInflater mInflater;//初始化界面
    private Context mContext;//上下文

    public ListViewAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);//初始化
    }

    public void setData(List<RecordBean> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        view = mInflater.inflate(R.layout.cell_list_view, null);

        RecordBean recordBean = (RecordBean) getItem(position);
        TextView remarkTV;
        TextView amountTV;
        TextView timeTV;
        ImageView categoryIcon;
        remarkTV = view.findViewById(R.id.textView_remark);
        amountTV = view.findViewById(R.id.textView_amount);
        timeTV = view.findViewById(R.id.textView_time);
        categoryIcon = view.findViewById(R.id.imageView_category);

        remarkTV.setText(recordBean.getRemark());
        if (recordBean.getType() == 1) {
            amountTV.setText("-" + recordBean.getAmount());
        } else {
            amountTV.setText("+" + recordBean.getAmount());
        }
        timeTV.setText(DateUtil.getFormattedTime(recordBean.getTimeStamp()));
        categoryIcon.setImageResource(GlobalUtil.getInstance().getResourceIcon(recordBean.getCategory()));
        return view;
    }
}