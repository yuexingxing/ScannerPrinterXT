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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iscan.exam.com.scannerprinterxt.data.BlueInfo;
import iscan.exam.com.scannerprinterxt.util.UIHelper;

public class MainActivity extends Activity {

    private String TAG = "zd";

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

        final String billcode1 = edtBillcode1.getText().toString();
        final String billcode2 = edtBillcode1.getText().toString();

        btnPrint.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
                startPrint();
                runOnUiThread(new Runnable() {
                    public void run() {
                        btnPrint.setEnabled(true);
                    }
                });
            }
        }).start();

    }


    private void printLable(ZebraPrinter printer, String billcode1, String billcode2) throws Exception {

        String fileName = "Test.LBL";
        File filepath = getFileStreamPath(fileName);
        FileOutputStream os = this.openFileOutput(fileName,
                Context.MODE_PRIVATE);
        PrinterLanguage pl = printer.getPrinterControlLanguage();

        int height = 0;
        int textSize = 30;
        int lineSize = 15;

        String barcode1 = "! U1 B 128 1 1 50 100 100 " + billcode1;//定义条码打印
        String barcode2 = "! U1 B 128 1 1 50 100 100 " + billcode2;//定义条码打印

        StringBuffer sb = new StringBuffer();
        sb.append("! 0 0 10  1\r\n");
        sb.append("ENCODING UTF-8\r\n");
        sb.append("CENTER\r\n");
        sb.append("SETMAG 1 2\r\n");

//        sb.append(barcode1);
        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 数量：" + "\r\n");
        height += textSize;
        sb.append("LEFT\r\n");
        sb.append("SETMAG 0 0\r\n");

        sb.append("TEXT GBUNSG24.CPF 0 0 " + billcode1);
        sb.append("TEXT GBUNSG24.CPF 0 300 " + height + " 日期：" + "\r\n");
        height += textSize;

        sb.append("TEXT GBUNSG24.CPF 0 300 " + height + " 签名：" + "\r\n");
        height += textSize;

        sb.append("LINE 0 " + height + " 580 " + (height) + " 1\r\n");// LINE

//        sb.append("   " + barcode2+ "\r\n");
        height += textSize;
        sb.append("TEXT GBUNSG24.CPF 0 30 " + height + billcode2 + "\r\n");
        height += textSize;

        int insertHeight = 9;
//        sb.insert(insertHeight, height);
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
//         String msg="! U1 JOURNAL\r\n"+ //禁用传感器纸张检测
//         "! U1 PAGE-WIDTH 600\r\n"+ //定义打印宽度
//         "! U1 ENCODING UTF-8\r\n"+ //定义数据编码方式
//         "! U1 SETLP GBUNSG24.CPF 0 24\r\n"+ //定义打印字体
//         "! U1 SETMAG 2 2\r\n"+ //定义字体缩放
//         "! U1 SETBOLD 1 \r\n"+ //设定字体加粗
//         "! U1 SETLF 25\r\n"+ //定义行间距
//         "欢迎光临\r\n"+"店名："+"四川成都\r\n"+ //定义打印数据内容
//         "客户:"+"张军\r\n"+
//         "! U1 CENTER\r\n"+ //定义打印居中
//         "! U1 B 128 1 1 50 100 100 123456ABC\r\n"+ //定义条码打印
//         "! U1 LEFT\r\n"; //定义靠左打印
        // ------------------测试------------------! UTILITIES „ PRINT
        String printTitle = "虹口足球场";// 标题
        String printStore = "广元市利州区中医院邮亭";// 店名
        String printClientName = "张建来";// 客户
        String printDate = new SimpleDateFormat("yyy-MM-dd").format(new Date());// 送货时间
        String printPhone = "13684336499";// 联系电话
        String printOrderId = "XGY30001301716";// 订单号
        String printAddress = "广元市利州区宝轮镇水电路中医院旁（面对大门）";// 地址
        String printCount = "20.00";// 合计香烟
        String printTotal = "2964.00";// 总价
        String printTotals = "贰仟玖佰陆拾肆元";// 总价大写
        String printUserName = "李志荣";// 送货人
        ArrayList<BaccoItemBean> printList = new ArrayList<BaccoItemBean>();// 订单详细
        printList.add(new BaccoItemBean("黄鹤楼（硬雪之景）", "999.00", "100.00",
                "1000.00"));
        printList.add(new BaccoItemBean("娇子（软红）", "344.00", "50.00", "500.00"));
        printList.add(new BaccoItemBean("娇子（红天之娇子）", "200.00", "80.00",
                "23040.00"));
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

        String billcode1 = "12345678";
        String billcode2 = "AAAAAAAA";

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

        sb.append("! U1 B 128 1 1 50 100 100 " + " 12345678 " + "\r\n");
        height += 50;

        sb.append("TEXT GBUNSG24.CPF 0 100 " + height + " " + billcode1 + " \r\n");//单号
        sb.append("TEXT GBUNSG24.CPF 0 400 " + height + " 数量：" + " \r\n");
        height += textSize;
        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 店名：" + printStore
                + "\r\n");
        height += textSize;

//        if (printAddress.length() > 20) {// 判断地址是否超过文本大写
//            sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 地址："
//                    + printAddress.substring(0, 20) + "\r\n");
//            height += textSize;
//            sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " "
//                    + printAddress.substring(20) + "\r\n");
//            height += textSize;
//        } else {
//            sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 地址：" + printAddress
//                    + "\r\n");
//            height += textSize;
//        }

//        sb.append("TEXT GBUNSG24.CPF 0 0 " + height + " 订单号：" + printOrderId
//                + "\r\n");
//        sb.append("TEXT GBUNSG24.CPF 0 300 " + height + " 送货日期：" + printDate
//                + "\r\n");
//        height += textSize;
//        sb.append("LINE 0 " + height + " 580 " + (height) + " 1\r\n");// LINE
        // [X]
        // [Y]
        // [EndX]
        // [EndY]
//        height += lineSize;
//        sb.append("TEXT GBUNSG24.CPF 0 20 " + height + " 品　　名\r\n");
//        sb.append("TEXT GBUNSG24.CPF 0 250 " + height + " 数量(条)\r\n");
//        sb.append("TEXT GBUNSG24.CPF 0 350 " + height + " 单价(元)\r\n");
//        sb.append("TEXT GBUNSG24.CPF 0 450 " + height + "   金 额\r\n");
//        height += textSize;
//        sb.append("LINE 0 " + height + " 580 " + (height) + " 1\r\n");

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
     * 打印
     */
    private void startPrint() {

        connection = new BluetoothConnection(tvMac.getText().toString());
        try {
            helper.showLoadingDialog("Sending file to printer ...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            // 相应打印方法
            // sprintFile(printer,"text.LBL");//打印文件
//             printFormat(printer);//模板打印
            printMyFormat(printer);// 自定义打印

//            printLable(printer, "123456", "QQQQQQQ");
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (ConnectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            helper.dismissLoadingDialog();
        }
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

    class BaccoItemBean {

        public BaccoItemBean(String name, String price, String count,
                             String total) {
            super();
            Name = name;
            Price = price;
            Count = count;
            Total = total;
        }

        public String Name;
        public String Price;
        public String Count;
        public String Total;
    }
}
