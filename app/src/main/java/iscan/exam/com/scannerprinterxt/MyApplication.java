package iscan.exam.com.scannerprinterxt;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

/**
 * yxx
 * <p>
 * ${Date} ${time}
 **/
public class MyApplication extends Application {

    public static final int PDA_SCAN_DATA = 0x9999;

    public static final String SCN_CUST_ACTION_SCODE = "com.android.server.scannerservice.broadcast";
    public static final String SCN_CUST_EX_SCODE = "scannerdata";

    public static Context instance;

    public void onCreate() {
        super.onCreate();

        instance = this;
        IntentFilter intentFilter = new IntentFilter(SCN_CUST_ACTION_SCODE);
        registerReceiver(mSamDataReceiver, intentFilter);
    }

    public static Context getInstance(){

        return instance;
    }

    private BroadcastReceiver mSamDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(SCN_CUST_ACTION_SCODE)) {

                String billcode;
                try {
                    billcode = intent.getStringExtra(SCN_CUST_EX_SCODE).toString();

                    Message message = new Message();
                    message.what = PDA_SCAN_DATA;
                    message.obj = billcode;
                    Log.v("zd", billcode + "");
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    Log.e("zd", e.toString());
                }
            }
        }
    };

}
