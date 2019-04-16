package com.bill.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bill.R;
import com.bill.bean.RecordBean;
import com.bill.util.GlobalUtil;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChartActivity extends AppCompatActivity {

    String TAG = "ChartActivity";
    private PieChart mPieChart;
    private boolean isType = true;
    private TextView type;
    private PieDataSet pieDataSet;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        initView();

        type = findViewById(R.id.typeChange);

        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isType) {
                    isType = false;
                    type.setText("支出");
                } else {
                    isType = true;
                    type.setText("收入");
                }
                initView();
            }
        });
    }

    @SuppressLint("NewApi")
    private void initView() {
        //饼状图
        mPieChart = findViewById(R.id.mPieChart);
        mPieChart.setUsePercentValues(true);//以百分比形式绘制
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(0, 0, 0, 0);

        //设置中间文件
        mPieChart.setDrawCenterText(true);
        mPieChart.setCenterTextSize(20f);
        mPieChart.setDrawHoleEnabled(true);//空心
        mPieChart.setHoleColor(Color.WHITE);
        mPieChart.setHoleRadius(50f);//以最大半径的百分比设置中心孔半径
        mPieChart.setTransparentCircleRadius(55f);//设置中心孔旁边透明圆半径
        mPieChart.setTransparentCircleColor(Color.WHITE);//设置透明圆颜色
        mPieChart.setTransparentCircleAlpha(120);//设置透明圆透明度
        //设置数据
        ArrayList<PieEntry> arrayList = new ArrayList<>();
        LinkedHashMap<String, Double> linkedHashMap = new LinkedHashMap<String, Double>();

        if (isType) {
            mPieChart.setCenterText("支出");
            for (RecordBean recordBean : GlobalUtil.records) {
                if (recordBean.getType() == 1) {
                    if (linkedHashMap.keySet().contains(recordBean.getCategory())) {
                        double temp = linkedHashMap.get(recordBean.getCategory());
                        temp = temp + recordBean.getAmount();
                        linkedHashMap.put(recordBean.getCategory(), temp);
                    } else {
                        linkedHashMap.put(recordBean.getCategory(), recordBean.getAmount());
                    }
                }
            }
        } else {
            mPieChart.setCenterText("收入");
            for (RecordBean recordBean : GlobalUtil.records) {
                if (recordBean.getType() == 2) {
                    if (linkedHashMap.keySet().contains(recordBean.getCategory())) {
                        double temp = linkedHashMap.get(recordBean.getCategory());
                        temp = temp + recordBean.getAmount();
                        linkedHashMap.put(recordBean.getCategory(), temp);
                    } else {
                        linkedHashMap.put(recordBean.getCategory(), recordBean.getAmount());
                    }
                }
            }
        }

        for (String key : linkedHashMap.keySet()) {
            arrayList.add(new PieEntry(linkedHashMap.get(key).floatValue(), key));
        }

        setData(arrayList);
        Legend legend = mPieChart.getLegend();//图例
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(0f);//设置水平轴间隙
        legend.setYEntrySpace(0f);//设置垂直轴间隙
        legend.setYOffset(0f);
        //输入标签样式
        mPieChart.setEntryLabelColor(Color.BLACK);
        mPieChart.setEntryLabelTextSize(15f);
    }

    private void setData(ArrayList<PieEntry> arrayList) {
        if (isType) {
            pieDataSet = new PieDataSet(arrayList, "支出");
        } else {
            pieDataSet = new PieDataSet(arrayList, "收入");
        }

        pieDataSet.setSliceSpace(0);//切片空隙
        pieDataSet.setSelectionShift(15f);
        //数据和颜色
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());//以百分比显示
        pieData.setValueTextSize(15f);//设置值文本大小
        pieData.setValueTextColor(Color.BLACK);//设置值文本颜色
        mPieChart.setData(pieData);
        mPieChart.highlightValues(null);
        //刷新
        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();
    }
}
