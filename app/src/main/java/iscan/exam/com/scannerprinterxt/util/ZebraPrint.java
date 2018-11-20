package iscan.exam.com.scannerprinterxt.util;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import iscan.exam.com.scannerprinterxt.data.Lable;

/**
 * Created by jamesh on 2015/3/27.
 */
public class ZebraPrint {

    private Context context;
    private UIHelper helper;

    public ZebraPrint(Context context) {

        Activity activity = (Activity) context;
        helper = new UIHelper(activity);
        this.context = context;
    }

    /**
     * 开始打印
     **/
    public void print(String mac, String billcode1, String billcode2) {

        Connection connection = new BluetoothConnection(mac);

        if (connection == null) {
            CommandTools.showToast("连接打印机失败");
            return;
        }

        try {
            helper.showLoadingDialog("打印中...");
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            sendPrintFile(printer, billcode1, billcode2);
            connection.close();
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

//TODO Testing
//    private static void sendPrintFile(ZebraPrinter printer) {
//        try {
////             使用文件打印
//            File filepath;
//            filepath =context.getFileStreamPath("PRINT.LBL");
//            createPrintFile(context, printer, "PRINT.LBL",objLable);
//            printer.sendFileContents(filepath.getAbsolutePath());
//
//            String strWrap="\r\n";
//
//            //直接打印字符
//            String cpclConfigLabel="! 0 200 200 500 1" +strWrap+
//                    "B QR 10 100 M 2 U 10" +strWrap+
//                    "M0A,QR code ABC123" +strWrap+
//                    "ENDQR" +strWrap+
//                    "T 4 0 10 400 QR code ABC123" +strWrap+
//                    "FORM" +strWrap+
//                    "PRINT";
//
//            printer.sendCommand(cpclConfigLabel);
//
//
//        } catch (ConnectionException e1) {
//            e1.printStackTrace();
//            // } catch (IOException e) {
//            //    e.printStackTrace();
//        }
//    }

    private void sendPrintFile(ZebraPrinter printer, String billcode1, String billcode2) {

        Lable objLable = new Lable();
        objLable.setCartonNo("123456");
        objLable.setItemID("345678");
        objLable.setLotNo("567890");
        objLable.setQty("12");
        objLable.setDate("2018-12-12");

        try {
            String ConfigLabel = "";

            PrinterLanguage pl = printer.getPrinterControlLanguage();
//            if (pl == PrinterLanguage.ZPL) {
//                ConfigLabel = buildZPL(objLable);
//            } else if (pl == PrinterLanguage.CPCL) {
            ConfigLabel = buildCPCL(objLable);
//            }

            printer.sendCommand(ConfigLabel);
        } catch (ConnectionException e1) {
            e1.printStackTrace();
        }
    }


    private static void createPrintFile(Context context, ZebraPrinter printer, String fileName, Lable objLable) {

        FileOutputStream os = null;
        try {
            os = context.openFileOutput(fileName, Context.MODE_PRIVATE);


            byte[] configLabel = null;

            PrinterLanguage pl = printer.getPrinterControlLanguage();
            if (pl == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
            } else if (pl == PrinterLanguage.CPCL) {
//        	String cpclConfigLabel ="! 0 200 200 1 1\r\nIN-INCHES\r\nT 5 0 0 0 1 cm = 0.3937\r\n\r\nIN-DOTS\r\nT 5 0 0 20 1 mm = 8 dots\r\n"+"B 39 1 1 20 0 40 UNITS\r\nT 5 0 0 60 UNITS\r\nFORM\r\nPRINT\r\n";

                String cpclConfigLabel = buildCPCL(objLable);

                configLabel = cpclConfigLabel.getBytes();
            }
            os.write(configLabel);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //TODO 128 Barcode
    private static String buildCPCL(Lable objLable) {
        String strCPCL = "";
        String strWrap = "\r\n";

        //TODO 1 :添加偏移量控制出纸。
        //BarcodeSystem_5.1.15.2008.1.apk
//        strCPCL+=lableStart(0.3, 400, 400, 3, 1) + strWrap;


//        strCPCL+="! UTILITIES" + strWrap+
//                "GAP-SENSE" +strWrap+
//                "SET-TOF 2" +strWrap+
//                "PRINT" ;

        strCPCL += lableStart(0, 400, 100, 3, 1) + strWrap;

        //TODO 2 :打印前添加40 dot-lines 。
        //The following example sets up the printer for pre-feeding 40 dot-lines prior to printing.
        //BarcodeSystem_5.1.15.2008.2.apk
//        strCPCL+="PREEFEED 40"+ strWrap;

        //TODO 3 :打印后添加40 dot-lines 。
        //BarcodeSystem_5.1.15.2008.3.apk
//        strCPCL+="POSTFEED 80"+ strWrap;

        //ItemID
        strCPCL += "IN-INCHES" + strWrap;//一定要
        strCPCL += printText(7, 0, 0, 2.75, "Item Code:" + objLable.getItemID()) + strWrap;
        strCPCL += "IN-DOTS" + strWrap;//一定要
        strCPCL += printBarCode("128", 1, 0, 30, 25, 530, objLable.getItemID()) + strWrap;
        //CartonNo
        strCPCL += "IN-INCHES" + strWrap;
        strCPCL += printText(7, 0, 0.3, 2.75, "Carton No:" + objLable.getCartonNo()) + strWrap;
        strCPCL += "IN-DOTS" + strWrap;
        strCPCL += printBarCode("128", 1, 0, 30, 89, 530, objLable.getCartonNo()) + strWrap;

        //LotNo
        strCPCL += "IN-INCHES" + strWrap;
        strCPCL += printText(7, 0, 0.61, 2.75, "S. Lot No:" + objLable.getLotNo()) + strWrap;
        strCPCL += "IN-DOTS" + strWrap;
        strCPCL += printBarCode("128", 1, 0, 30, 152, 530, objLable.getLotNo()) + strWrap;

        //SuppInvNo
//            strCPCL += "IN-INCHES" + strWrap;
//            strCPCL += printText(7, 0, 0.92, 2.75, "S. Inv. No:" + objLable.getSuppInvNo()) + strWrap;
//            strCPCL += "IN-DOTS" + strWrap;
//            strCPCL += printBarCode("128", 1, 0, 30, 217, 530, objLable.getSuppInvNo()) + strWrap;
//            //Qty
//            strCPCL += "IN-INCHES" + strWrap;
//            strCPCL += printText(7, 0, 1.25, 2.75, "Qty:" + objLable.getQty()) + strWrap;
//            strCPCL += "IN-DOTS" + strWrap;
//            strCPCL += printBarCode("128", 1, 0, 30, 286, 530, objLable.getQty()) + strWrap;
//            //Date
//            strCPCL += "IN-INCHES" + strWrap;
//            strCPCL += printText(7, 0, 1.56, 2.75, "Date Code:" + objLable.getDate()) + strWrap;
//            strCPCL += "IN-DOTS" + strWrap;
//            strCPCL += printBarCode("128", 1, 0, 30, 344, 530, objLable.getDate()) + strWrap;
//
//            strCPCL += "IN-INCHES" + strWrap;
//            strCPCL += printText(7, 0, 1.70, 0.5, GetNowDate()) + strWrap;


        strCPCL += "IN-DOTS" + strWrap;
        //TODO 4 :等待1/4秒，如果没有内容打印则提前80点线推进媒体 。
        strCPCL += "PRESENT-AT 30 4" + strWrap;

        strCPCL += "FORM" + strWrap;

        strCPCL += "PRINT" + strWrap;

        return strCPCL;
    }


    private static String buildZPL(Lable objLable) {
        String strZPL = "";
        String strWrap = "\r\n";

        strZPL += lableStart_ZPL(30, 100, 30);

        //ItemID
        strZPL += printText_ZPL(100, 35, "Item Code:" + objLable.getItemID()) + strWrap;
        strZPL += printCode128_ZPL(100, 70, 40, objLable.getItemID()) + strWrap;
        //CartonNo
        strZPL += printText_ZPL(100, 125, "Carton No:" + objLable.getCartonNo()) + strWrap;
        strZPL += printCode128_ZPL(100, 160, 40, objLable.getCartonNo()) + strWrap;

        //LotNo
//        strZPL += printText_ZPL(100, 215, "S. Lot No:" + objLable.getLotNo()) + strWrap;
//        strZPL += printCode128_ZPL(100, 250, 40, objLable.getLotNo()) + strWrap;
//
//        //SuppInvNo
//        strZPL += printText_ZPL(100, 305, "S. Inv. No:" + objLable.getSuppInvNo()) + strWrap;
//        strZPL += printCode128_ZPL(100, 340, 40, objLable.getSuppInvNo()) + strWrap;
//        //Qty
//        strZPL += printText_ZPL(100, 355, "Qty:" + objLable.getQty()) + strWrap;
//        strZPL += printCode128_ZPL(100, 390, 40, objLable.getQty()) + strWrap;
//        //Date
//        strZPL += printText_ZPL(100, 445, "Date Code:" + objLable.getDate()) + strWrap;
//        strZPL += printCode128_ZPL(100, 480, 40, objLable.getDate()) + strWrap;
//
//        strZPL += printText_ZPL(1100, 500, GetNowDate()) + strWrap;


        strZPL += lableEnd_ZPL();

        return strZPL;
    }


    //TODO backup
//    private static String buildCPCL(Lable objLable){
//        String strCPCL="";
//        String strWrap="\r\n";
//        String strValue="";
//
//        strCPCL+=lableStart(0, 400, 400,3, 1) + strWrap;
//
//        //ItemID
//        strCPCL+="IN-INCHES"+ strWrap;//一定要
//        strCPCL+=printText(7, 0, 0, 2.8, "Item Code:" + objLable.getItemID()) + strWrap;
//        strCPCL+="IN-DOTS"+ strWrap;//一定要
//        strCPCL+=printBarCode("39", 1, 0, 30, 25, 580, objLable.getItemID()) + strWrap;
//
//        strValue+=objLable.getItemID()+" ";
//        //CartonNo
//        strCPCL+="IN-INCHES"+ strWrap;
//        strCPCL+=printText(7, 0, 0.3, 2.8, "Carton No:" + objLable.getCartonNo()) + strWrap;
//        strCPCL+="IN-DOTS"+ strWrap;
//        strCPCL+=printBarCode("39", 1, 0, 30, 89, 580, objLable.getCartonNo()) + strWrap;
//
//        strValue+=objLable.getCartonNo()+" ";
//
//
//        if (objLable.getLotNo() != "----"){
//            //LotNo
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 0.61, 2.8, "S. Lot No:" + objLable.getLotNo()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 152, 580, objLable.getLotNo()) + strWrap;
//
//            strValue+=objLable.getLotNo().trim()+" ";
//
//            //SuppInvNo
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 0.92, 2.8, "S. Inv. No:" + objLable.getSuppInvNo()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 217, 580, objLable.getSuppInvNo()) + strWrap;
//
//            strValue+=objLable.getSuppInvNo()+" ";
//            //Qty
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 1.25, 2.8, "Qty:" + objLable.getQty()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 286, 580, objLable.getQty()) + strWrap;
//
//            strValue+=objLable.getQty()+" ";
//            //Date
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 1.56, 2.8, "Date Code:" + objLable.getDate()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 344, 580, objLable.getDate()) + strWrap;
//
//            strValue+=objLable.getDate();
//
////            strCPCL+="IN-INCHES"+ strWrap;
////            strCPCL+=printText(7, 0, 1.70, 0.5,GetNowDate()) + strWrap;
//
//
//        }else{
//            strValue+=objLable.getLotNo()+" ";
//
//            //SuppInvNo
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 0.61, 2.8, "S. Inv. No:" + objLable.getSuppInvNo()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 152, 580, objLable.getSuppInvNo()) + strWrap;
//
//            strValue+=objLable.getSuppInvNo()+" ";
//            //Qty
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 0.92, 2.8, "Qty:" + objLable.getQty()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 217, 580, objLable.getQty()) + strWrap;
//
//            strValue+=objLable.getQty()+" ";
//            //Date
//            strCPCL+="IN-INCHES"+ strWrap;
//            strCPCL+=printText(7, 0, 1.25, 2.8, "Date Code:" + objLable.getDate()) + strWrap;
//            strCPCL+="IN-DOTS"+ strWrap;
//            strCPCL+=printBarCode("39", 1, 0, 30, 286, 580, objLable.getDate()) + strWrap;
//
//            strValue+=objLable.getDate();
//
//        }
//
//        strCPCL+="IN-INCHES"+ strWrap;
//        strCPCL+=printText(7, 0, 1.70, 0.5,GetNowDate()) + strWrap;
//
//        strCPCL+="IN-DOTS"+ strWrap;
//        strCPCL+=printQRCode(200,160,strValue) + strWrap;
//
//        strCPCL+="VB QR 120 240 M 2 U 10" +strWrap+
//                 "M0A,QR code ABC123" +strWrap+
//                 "ENDQR" +strWrap;
//
//        strCPCL+="FORM"+strWrap;
//        strCPCL+="PRINT"+strWrap;
//
//        return strCPCL;
//    }
    //TODO

    public static String GetNowDate() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        temp_str = sdf.format(dt);
        return temp_str;
    }


    //region CPCL

    /**
     * @param offset     偏移
     * @param Horizontal 水平尺寸
     * @param Vertical   垂直尺寸
     * @param height     高度
     * @param qty        数量
     * @return
     */
    private static String lableStart(double offset, float Horizontal, float Vertical, double height, int qty) {
        String strLable = "!" + " " + String.valueOf(offset) + " " + String.valueOf(Horizontal) + " " +
                String.valueOf(Vertical) + " " + String.valueOf(height) + " " + String.valueOf(qty);

        return strLable;
    }

    /**
     * @param font     字体
     * @param size     字号
     * @param x        起始水平坐标
     * @param y        起始垂直坐标
     * @param strValue 内容
     * @return
     */
    private static String printText(int font, double size, double x, double y, String strValue) {
        String strText = "VT" + " " + String.valueOf(font) + " "
                + String.valueOf(size) + " " + String.valueOf(x) + " "
                + String.valueOf(y) + " " + strValue;

        return strText;
    }

    private static String printBox(int font, double size, double x, double y, String strValue) {
        String strText = "VT" + " " + String.valueOf(font) + " "
                + String.valueOf(size) + " " + String.valueOf(x) + " "
                + String.valueOf(y) + " " + strValue;

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
        String strBarCode = "VB" + " " + type + " " + String.valueOf(width) + " "
                + String.valueOf(ratio) + " " + String.valueOf(height) + " " + String.valueOf(x) + " "
                + String.valueOf(y) + " " + strValue;

        return strBarCode;
    }

    /**
     * @param x        起始水平坐标
     * @param y        起始垂直坐标
     * @param strValue 内容
     * @return
     */
    private static String printQRCode(int x, int y, String strValue) {
        /**
         * M: QR code model No.Range is 1 or 2.Default is 2.
         * U: Unit-width/Unit-height of the module.Range is 1 to 32.Default is 6.
         * "M" is the error correction parameter (L=Low, M=Medium, Q=Medium High, H=High)
         * "0"  is the mask pattern
         * "A" is the mode conversion (A=Auto, M=Manual)
         */

        String strWrap = "\r\n";
        String strBarCode = "VB" + " QR " + String.valueOf(x) + " " + String.valueOf(y) + " M 2 " + " U 4 " + strWrap +
                "M0A," + strValue + strWrap +
                "ENDQR";

        return strBarCode;
    }

    //endregion

    //region ZPL


    private static String lableStart_ZPL(int md, int x, int y) {
        /**
         * ^XA              指令块的开始
         * ^MD              设置色带颜色的深度,取值范围从-30到30,上面的示意指令将颜色调到了最深.
         * ^LH              设置条码纸的边距的.
         * ^CF              改变字符字体默认字体
         */

        String strLable = "^XA" + "^CF0,15,7" + "^MD" + String.valueOf(md) + "^LH" + String.valueOf(x) + "," + String.valueOf(y);

        return strLable;
    }


    /**
     * @param x        起始水平坐标
     * @param y        起始垂直坐标
     * @param strValue 内容
     * @return
     */
    private static String printText_ZPL(int x, int y, String strValue) {

        String strText = "^FO" + String.valueOf(x) + "," + String.valueOf(y) +
                "^AD" +
                "^FD" + strValue + "^FS";

        return strText;
    }

    private static String printCode128_ZPL(int x, int y, int height, String strValue) {
        /**
         * ^FO20,10              ^FO是设置条码左上角的位置的,0,0代表完全不留边距.
         * ^ACN,18,10            ^ACN是设置字体的.因为在条码下方会显示该条码的内容,所以要设一下字体.这个跟条码无关.
         * ^BY1.4,3,50           ^BY是设置条码样式的,1.4是条码的缩放级别,3是条码中粗细柱的比例,50是条码高度.
         * ^BCN,,Y,N             ^BC是打印code128的指令,具体参数详见ZPL的说明书(百度云盘)
         * ^FD01008D004Q-0^FS    ^FD设置要打印的内容, ^FS表示换行.
         */

        String strBarCode = "^FO" + String.valueOf(x) + "," + String.valueOf(y) +
                "^BC" + "N" + "," + String.valueOf(height) + ",N,N,N" +
                "^FD" + strValue + "^FS";

        return strBarCode;
    }

    private static String lableEnd_ZPL() {
        String strBarCode = "^XZ";

        return strBarCode;
    }


    //endregion

}

