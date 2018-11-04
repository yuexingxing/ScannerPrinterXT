package iscan.exam.com.scannerprinterxt;

import android.widget.Toast;

/**
 * yxx
 * <p>
 * ${Date} ${time}
 **/
public class CommandTools {

    public static void showToast(String msg){

        Toast.makeText(MyApplication.getInstance(), msg + "", Toast.LENGTH_SHORT).show();
    }
}
