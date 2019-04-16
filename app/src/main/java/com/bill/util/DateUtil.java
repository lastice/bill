package com.bill.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getFormattedTime(long timeStamp) {

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(new Date(timeStamp));
    }

    public static String getFormattedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date());
    }
}