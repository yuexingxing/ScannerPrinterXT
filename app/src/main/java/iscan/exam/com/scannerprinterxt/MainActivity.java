package iscan.exam.com.scannerprinterxt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import java.util.List;
import java.util.Set;

import iscan.exam.com.scannerprinterxt.data.BlueInfo;
import iscan.exam.com.scannerprinterxt.util.CommandTools;
import iscan.exam.com.scannerprinterxt.util.UIHelper;

/**
 * 主界面
 */
public class MainActivity extends Activity {

    private String TAG = "zd";
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

        final String billcode1 = edtBillcode1.getText().toString();
        final String billcode2 = edtBillcode2.getText().toString();

        if (TextUtils.isEmpty(billcode1) || TextUtils.isEmpty(billcode2)) {
            CommandTools.showToast("单号不能为空");
            return;
        }

//        final String billcode1 = "P1234568";
//        final String billcode2 = "Z8234229-2-1-1-003";

        btnPrint.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {

                startPrint(billcode1, billcode2);
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

        boolean result = true;
        connection = new BluetoothConnection(tvMac.getText().toString());
        try {

            helper.showLoadingDialog("打印中...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            if (printer == null) {
                showToast("请检查打印机是否正确连接");
                return;
            }

            printMyFormat(printer, billcode1, billcode2);
//            printMyFormat(printer);
        } catch (ConnectionException e) {
            result = false;
            helper.showErrorDialogOnGuiThread("未找到打印机，请检查打印机是否连接正常");
        } catch (ZebraPrinterLanguageUnknownException e) {
            result = false;
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {

                    if (result) {
                        mHandler.sendEmptyMessage(1001);
                    }
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
    private void printMyFormat(ZebraPrinter printer) throws Exception {
        String fileName = "Test.LBL";
        File filepath = getFileStreamPath(fileName);
        FileOutputStream os = this.openFileOutput(fileName,
                Context.MODE_PRIVATE);
        PrinterLanguage pl = printer.getPrinterControlLanguage();
        // ----------------打印Test+边框--------------------
        // String msg="! 0 200 200 406 1\r\n"
        // + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n"
        // + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
        // ----------------打印二维码--------------------
        // String msg="! 0 200 200 500 1\r\n"+//二维码ERR
        // "B QR 10 100 M 2 U 10\r\n"+
        // "MA,QR code ABC123\r\n"+
        // "ENDQR\r\n"+
        // "T 4 0 10 400 QR code ABC123\r\n"+
        // "FORM\r\n"+
        // "PRINT\r\n";
        // ------------------行模式打印------------------
        // String msg="! U1 JOURNAL\r\n"+ //禁用传感器纸张检测
        // "! U1 PAGE-WIDTH 600\r\n"+ //定义打印宽度
        // "! U1 ENCODING UTF-8\r\n"+ //定义数据编码方式
        // "! U1 SETLP GBUNSG24.CPF 0 24\r\n"+ //定义打印字体
        // "! U1 SETMAG 2 2\r\n"+ //定义字体缩放
        // "! U1 SETBOLD 1 \r\n"+ //设定字体加粗
        // "! U1 SETLF 25\r\n"+ //定义行间距
        // "欢迎光临\r\n"+"店名："+"四川成都\r\n"+ //定义打印数据内容
        // "客户:"+"张军\r\n"+
        // "! U1 CENTER\r\n"+ //定义打印居中
        // "! U1 B 128 1 1 50 100 100 123456ABC\r\n"+ //定义条码打印
        // "! U1 LEFT\r\n"; //定义靠左打印
        // ------------------测试------------------! UTILITIES „ PRINT
        String printTitle = "四川省烟草公司广元市公司销售单据";// 标题
        String printStore = "广元市利州区中医院邮亭";// 店名
        String printClientName = "张建来";// 客户
        String printDate = new SimpleDateFormat("yyy-MM-dd").format(new Date());// 送货时间
        String printPhone = "13684336499";// 联系电话
        String printOrderId = "XGY30001301716";// 订单号
        String printAddress = "广元市利州区宝轮镇水电路中医院旁（面对大门）";// 地址

        // Print whith:7.62CM height:auto
        // String msg =
        // "! U1 ENCODING UTF-8\r\n"+printTitle+"\r\n! U1 CENTER \r\n";
        // String msg="! 0 200 200 106 1\r\n"
        // + "ON-FEED IGNORE\r\n" + "BOX 20 20 700 30 8\r\n"
        // + "T 0 6 137 177 TEST\r\n"
        // + "PRINT\r\n";
        // String msg="!0 200 200 106 1\r\n ! U1 CENTER \r\n"+
        // printTitle+"\r\n";
        // String msg=""+

        // //居中文本，及字体放大
        // "! 0 20 0 60 1\r\n"+
        // printTitle+"\r\n"+
        // "ENCODING UTF8\r\n"+
        // "COUNTRY CHINA\r\n"+
        // // "TEXT GBUNSG24.CPF 0 20 30 倀倁倂倃倄倅倆倇\r\n"+
        // // "ENCODING ASCII\r\n"+
        // "CENTER\r\n"+
        // // "SETMAG 2 2\r\n"+
        // "TEXT 0 0 0 10 "+printTitle+"\r\n"+
        // // "SETMAG 0 0\r\n"+
        // "FORM\r\n"+
        // "PRINT\r\n";
        // TEXT GBUNSG24.CPF 0 20 20 你好 1：字体2:未知3:左4:上5:输出
        // ! 0 0 50 100 1 1：、2：、3:字号有关、4:高度、5：
        int height = 0;
        int textSize = 30;
        int lineSize = 15;
        StringBuffer sb = new StringBuffer();
        int insertHeight = 9;
        sb.append("! 0 0 10  1\r\n");
//		int insertHeight=11;
//		sb.append("!0 200 200  1\r\n");
        sb.append("ENCODING UTF-8\r\n");
        sb.append("CENTER\r\n");
        sb.append("SETMAG 1 2\r\n");

        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " " + printTitle + "\r\n");// 标题
        height += 55;
        sb.append("LEFT\r\n");
        sb.append("SETMAG 0 0\r\n");

        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 姓名：" + printClientName
                + "\r\n");
        sb.append("TEXT GBUNSG24.CPF 0 300 " + height + " 电话：" + printPhone
                + "\r\n");
        height += textSize;
        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 店名：" + printStore
                + "\r\n");
        height += textSize;

        if (printAddress.length() > 20) {// 判断地址是否超过文本大写
            sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 地址："
                    + printAddress.substring(0, 20) + "\r\n");
            height += textSize;
            sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " "
                    + printAddress.substring(20) + "\r\n");
            height += textSize;
        } else {
            sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 地址：" + printAddress
                    + "\r\n");
            height += textSize;
        }

        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 订单号：" + printOrderId
                + "\r\n");
        sb.append("TEXT GBUNSG24.CPF 0 300 " + height + " 送货日期：" + printDate
                + "\r\n");
        height += textSize;
        sb.append("LINE 0 " + height + " 580 " + (height) + " 1\r\n");// LINE
        // [X]
        // [Y]
        // [EndX]
        // [EndY]
        height += lineSize;
        sb.append("TEXT GBUNSG24.CPF 0 20 " + height + " 品　　名\r\n");
        sb.append("TEXT GBUNSG24.CPF 0 250 " + height + " 数量(条)\r\n");
        sb.append("TEXT GBUNSG24.CPF 0 350 " + height + " 单价(元)\r\n");
        sb.append("TEXT GBUNSG24.CPF 0 450 " + height + "   金 额\r\n");
        height += textSize;
        sb.append("LINE 0 " + height + " 580 " + (height) + " 1\r\n");
        height += lineSize;

        sb.insert(insertHeight, height);
        sb.append("FORM\r\n");
        sb.append("PRINT\r\n");

        if (pl == PrinterLanguage.ZPL) {
            System.out.println("ZPL");
        } else if (pl == PrinterLanguage.CPCL) {
            System.out.println("CPCL");
        }
        os.write(sb.toString().getBytes());
        os.flush();
        os.close();
        printer.sendFileContents(filepath.getAbsolutePath());// 打印文件
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

        String billcodeFirst;
        String billcodeSenond;

        char char1First = billcode1.charAt(0);
        if (char1First >= 'A' && char1First <= 'Z' || (char1First >= 'a' && char1First <= 'z')) {
            billcodeFirst = billcode1.substring(1, billcode1.length());
        } else {
            billcodeFirst = billcode1;
        }

        char char2First = billcode2.charAt(0);
        if ((char2First >= 'A' && char2First <= 'Z') || (char2First >= 'a' && char2First <= 'z')) {
            billcodeSenond = billcode2.substring(1, billcode2.length());
        } else {
            billcodeSenond = billcode2;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("! 0 600 400 300 1\r\n");
        sb.append("ENCODING UTF-8\r\n");
        sb.append("SETMAG 0 0\r\n");
        sb.append("ON-FEED IGNORE\r\n");

        int lineHeight = 50;
        int totalHeight = 0;

        //短条码---数量
        sb.append(printBarCode("128", 1, 0, 40, 80, totalHeight, billcode1));
        sb.append(printBox(320, 0, 550, 40, 1));
        sb.append(printText(330, totalHeight + 10, "数量:"));

        totalHeight += lineHeight;

        //条码---日期
        sb.append("SETMAG 1 2\r\n");
        sb.append(printText(120, totalHeight, billcodeFirst));
        sb.append("LEFT\r\n");
        sb.append(printBox(320, totalHeight, 550, totalHeight + 40, 1));
        sb.append("SETMAG 0 0\r\n");
        sb.append(printText(330, totalHeight + 10, "日期:  " + CommandTools.getDate()));

        totalHeight += lineHeight;

        //---签名
        sb.append(printBox(320, totalHeight, 550, totalHeight + 40, 1));
        sb.append(printText(330, totalHeight + 10, "签名:"));

        totalHeight += lineHeight;

        //长条码---
        sb.append(printBarCode("128", 1, 0, 30, 80, totalHeight, billcode2));
        sb.append("CENTER\r\n");
        sb.append(printText(0, totalHeight + 40, billcodeSenond));

        sb.append("FORM\r\n");
        sb.append("PRINT\r\n");

        os.write(sb.toString().getBytes());
        os.flush();
        os.close();
        printer.sendFileContents(filepath.getAbsolutePath());// 打印文件
    }

    /**
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
    private static String printText(double x, double y, String strValue) {
        String strText = "TEXT" + " " + String.valueOf("GBUNSG24.CPF") + " "
                + String.valueOf(0) + " " + String.valueOf(x) + " "
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

    public Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            if (msg.what == 1001) {
                edtBillcode1.setText("");
                edtBillcode2.setText("");
                edtBillcode1.requestFocus();
            }
        }
    };

    public void showToast(final String content) {

        runOnUiThread(new Runnable() {
            public void run() {

                CommandTools.showToast(content);
            }
        });
    }
}
