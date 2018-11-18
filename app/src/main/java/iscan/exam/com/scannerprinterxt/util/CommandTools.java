package iscan.exam.com.scannerprinterxt.util;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import iscan.exam.com.scannerprinterxt.MyApplication;

/**
 * yxx
 * <p>
 * ${Date} ${time}
 **/
public class CommandTools {

    public static String getDate() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static void showToast(String msg) {

        Toast.makeText(MyApplication.getInstance(), msg + "", Toast.LENGTH_SHORT).show();
    }
}
