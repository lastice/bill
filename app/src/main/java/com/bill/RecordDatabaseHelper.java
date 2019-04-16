package com.bill;

import com.bill.bean.RecordBean;
import com.bill.util.DateUtil;
import com.bill.util.GlobalUtil;

import java.util.LinkedList;


public class RecordDatabaseHelper {

    static String TAG = "RecordDatabaseHelper";

    public LinkedList<RecordBean> readRecords(String date) {

        LinkedList<RecordBean> recordBeans = new LinkedList<>();

        for (RecordBean recordBean : GlobalUtil.records) {
            if (recordBean.getDate().equals(date)) {
                recordBeans.add(recordBean);
            }
        }

        return recordBeans;
    }

    public LinkedList<String> getAvailableDate() {

        LinkedList<String> dates = new LinkedList<>();

        for (RecordBean recordBean : GlobalUtil.records) {
            if (!dates.contains(recordBean.getDate())) {
                dates.add(recordBean.getDate());
            }
        }

        if (!dates.contains(DateUtil.getFormattedDate())) {
            dates.add(DateUtil.getFormattedDate());
        }

        return dates;
    }
}
