package iscan.exam.com.scannerprinterxt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iscan.exam.com.scannerprinterxt.data.BlueInfo;
import iscan.exam.com.scannerprinterxt.util.CommandTools;
import iscan.exam.com.scannerprinterxt.util.PrintUtil;
import iscan.exam.com.scannerprinterxt.util.UIHelper;
import iscan.exam.com.scannerprinterxt.util.ZebraPrint;

/**
 * 主界面
 */
public class MainActivity extends Activity {

    private String TAG = "zd";
    String strWrap = "\r\n";
    private long exitTime = 0;
    private List<BlueInfo> blueList = new ArrayList<>();
    private List<String> blueListMac = new ArrayList<>();
    private Connection connection;

    private EditText edtBillcode1;
    private EditText edtBillcode2;
    private Button btnPrint;
    private TextView tvMac;
    private Spinner spinner;

    private ArrayAdapter<String> adapterMac;
    private UIHelper helper;// Dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        initBlueTooth();
    }

    private void findView() {

        helper = new UIHelper(this);
        edtBillcode1 = findViewById(R.id.billcode1);
        edtBillcode2 = findViewById(R.id.billcode2);
        btnPrint = findViewById(R.id.print);
        tvMac = findViewById(R.id.mac);
        spinner = findViewById(R.id.spinner);

        edtBillcode1.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Log.v("zd", "条码1前：" + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Log.v("zd", "条码1后：" + charSequence.toString());

                if (TextUtils.isEmpty(charSequence.toString())) {
                    return;
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //execute the task

                        edtBillcode2.requestFocus();
                    }
                }, 1000);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtBillcode2.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Log.v("zd", "条码2前：" + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Log.v("zd", "条码2后：" + charSequence.toString());

                if (TextUtils.isEmpty(charSequence.toString())) {
                    return;
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        //execute the task

                        edtBillcode1.requestFocus();
                    }
                }, 500);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtBillcode1.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    edtBillcode1.setText("");
                } else {
                    // 此处为失去焦点时的处理内容
                }
            }
        });

        edtBillcode2.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    edtBillcode2.setText("");
                } else {
                    // 此处为失去焦点时的处理内容
                }
            }
        });

        // 声明一个ArrayAdapter用于存放简单数据
        adapterMac = new ArrayAdapter<String>(
                MainActivity.this, R.layout.item_drop,
                blueListMac);
        // 把定义好的Adapter设定到spinner中
        spinner.setAdapter(adapterMac);

        //为Spinner设定选中事件
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 在选中之后触发
                tvMac.setText(blueListMac.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 这个一直没有触发，我也不知道什么时候被触发。
                //在官方的文档上说明，为back的时候触发，但是无效，可能需要特定的场景
            }
        });

        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                checkPrint();
            }
        });
    }

    //检查打印
    public void checkPrint() {

//        edtBillcode1.setText("1234567890");
//        edtBillcode2.setText("ASDFASDFASF");

//        final String billcode1 = edtBillcode1.getText().toString();
//        final String billcode2 = edtBillcode1.getText().toString();

        final String billcode1 = "P1234567";
        final String billcode2 = "Z8234229-2-1-1-004";

        btnPrint.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {

                String billcodeFirst = billcode1;
                String billcodeSenond = billcode2;

                char char1First = billcodeFirst.charAt(0);
                if (char1First >= 'A' && char1First <= 'Z' || (char1First >= 'a' && char1First <= 'z')) {
                    billcodeFirst = billcodeFirst.substring(1, billcodeFirst.length());
                }

                char char2First = billcodeSenond.charAt(0);
                if ((char2First >= 'A' && char2First <= 'Z') || (char2First >= 'a' && char2First <= 'z')) {
                    billcodeSenond = billcodeSenond.substring(1, billcodeSenond.length());
                }

                startPrint(billcodeFirst, billcodeSenond);
                runOnUiThread(new Runnable() {
                    public void run() {
                        btnPrint.setEnabled(true);
                    }
                });
            }
        }).start();

    }

    private void initBlueTooth() {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                adapter.enable();
                //sleep one second ,avoid do not discovery
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                CommandTools.showToast("请现开启蓝牙，否则将无法使用打印功能");
                return;
            }

            blueList.clear();
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            Log.d(TAG, "获取已经配对devices" + devices.size());
            for (BluetoothDevice bluetoothDevice : devices) {
                Log.d(TAG, "已经配对的蓝牙设备：");

                BlueInfo info = new BlueInfo();
                info.setName(bluetoothDevice.getName());
                info.setMac(bluetoothDevice.getAddress());

                blueList.add(info);
                blueListMac.add(bluetoothDevice.getAddress());
            }

            if (blueListMac.size() > 0) {
                tvMac.setText(blueListMac.get(0));
            }

            adapterMac.notifyDataSetChanged();
        } else {
            CommandTools.showToast("本机没有蓝牙设备");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * 打印
     */
    private void startPrint(String billcode1, String billcode2) {

        connection = new BluetoothConnection(tvMac.getText().toString());
        try {

            helper.showLoadingDialog("打印中...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            printMyFormat(printer, billcode1, billcode2);
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread("未找到打印机，请检查打印机是否连接正常");
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
            helper.dismissLoadingDialog();
        }
    }

    /**
     * 自定义打印
     */
    private void printMyFormat(ZebraPrinter printer, String billcode1, String billcode2) throws Exception {

        String fileName = "Test.LBL";
        File filepath = getFileStreamPath(fileName);
        FileOutputStream os = this.openFileOutput(fileName, Context.MODE_PRIVATE);
        PrinterLanguage pl = printer.getPrinterControlLanguage();

        if (pl == PrinterLanguage.ZPL) {
            System.out.println("ZPL");
        } else if (pl == PrinterLanguage.CPCL) {
            System.out.println("CPCL");
        }

        StringBuffer sb = new StringBuffer();
        sb.append("! 0 600 400 400 1\r\n");
        sb.append("ON-FEED IGNORE\r\n");

        sb.append(printBarCode("128", 1, 0, 40, 80, 50, billcode1));
        sb.append(printBox(320, 50, 550, 90, 1));
        sb.append(printText(5, 1, 330, 60, "size:"));

        //条码---日期
        sb.append(printText(4, 0, 80, 100, billcode1));
        sb.append(printBox(320, 100, 550, 140, 1));
        sb.append(printText(5, 0, 330, 110, "date:   " + CommandTools.getDate()));

        //签名
        sb.append(printBox(320, 150, 550, 190, 1));
        sb.append(printText(5, 3.55, 330, 160, "sign:"));

        //长条码
        sb.append(printBarCode("128", 1, 0, 30, 100, 210, billcode2));
        sb.append(printText(5, 2.8, 180, 250, billcode2));

        sb.append("PRINT\r\n");

        os.write(sb.toString().getBytes());
        os.flush();
        os.close();
        printer.sendFileContents(filepath.getAbsolutePath());// 打印文件
    }

    /**
     * @param font     字体
     * @param size     字号
     * @param x        起始水平坐标
     * @param y        起始垂直坐标
     * @param strValue 内容
     * @return 按照公司maven库内bluetoothlibrary内包装的结果,
     * 字体大小设置分别代表的字体size为:
     * 1 ==> 3
     * 0 ==> 8
     * 3,55 ==> 16
     * 2,8 ==> 24
     * 4 ==> 32
     * 默认字体大小为24.
     * 然后TEXT后第二位为对应的字号后的大小,默认为0就行.想再加大,++就行.
     * 比如说想要设置 比常规字体大一号,且再加大,就是
     * TEXT 4 1 x y 例子
     */
    private static String printText(int font, double size, double x, double y, String strValue) {
        String strText = "T" + " " + String.valueOf(font) + " "
                + String.valueOf(size) + " " + String.valueOf(x) + " "
                + String.valueOf(y) + " " + strValue + "\r\n";

        return strText;
    }

    /**
     * BOX ：打印方框
     * {x0 }:左上角横向坐标
     * {y0 }:左上角纵向坐标
     * {x1}:右下角横向坐标
     * {y1}:右上角纵向坐标
     * {width}: 线条宽度
     */
    private static String printBox(double x, double y, double width, double height, int border) {
        String strText = "BOX" + " " + String.valueOf(x) + " "
                + String.valueOf(y) + " " + String.valueOf(width) + " "
                + String.valueOf(height) + " " + border + "\r\n";

        return strText;
    }

    /**
     * @param type     条码字体类型（例如：39,128,UPCA,UPCE,EAN13,EAN8,I2OF5,UCCEAN128,MSI,POSTNET,FIM）
     * @param width    条码宽度
     * @param ratio    宽窄比（例如：0 = 1.5 : 1，1 = 2.0 : 1，20 = 2.0:1，30 = 3.0:1）
     * @param height   高度
     * @param x        起始水平坐标
     * @param y        起始垂直坐标
     * @param strValue 内容
     * @return
     */
    private static String printBarCode(String type, int width, int ratio, int height, int x, int y, String strValue) {
        String strBarCode = "B" + " " + type + " " + String.valueOf(width) + " "
                + String.valueOf(ratio) + " " + String.valueOf(height) + " " + String.valueOf(x) + " "
                + String.valueOf(y) + " " + strValue + "\r\n";

        return strBarCode;
    }

    public void printDemo4(ZebraPrinter printer) throws Exception {

        String fileName = "Test.LBL";
        File filepath = getFileStreamPath(fileName);
        FileOutputStream os = this.openFileOutput(fileName, Context.MODE_PRIVATE);

        os.write(getConfigLabel(printer));
        os.flush();
        os.close();
        printer.sendFileContents(filepath.getAbsolutePath());// 打印文件
    }

    /*
     * Returns the command for a test label depending on the printer control language
     * The test label is a box with the word "TEST" inside of it
     *
     * _________________________
     * |                       |
     * |                       |
     * |        TEST           |
     * |                       |
     * |                       |
     * |_______________________|
     *
     *
     */
    private byte[] getConfigLabel(ZebraPrinter printer) {

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 100 80 2\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }
}
