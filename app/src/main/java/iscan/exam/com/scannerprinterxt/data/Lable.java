package iscan.exam.com.scannerprinterxt.data;

/**
 * yxx
 * <p>
 * ${Date} ${time}
 **/
public class Lable {

    private String itemID;
    private String cartonNo;
    private String lotNo;
    private String suppInvNo;
    private String qty;
    private String date;

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemId) {
        this.itemID = itemId;
    }

    public String getCartonNo() {
        return cartonNo;
    }

    public void setCartonNo(String cartonNo) {
        this.cartonNo = cartonNo;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public String getSuppInvNo() {
        return suppInvNo;
    }

    public void setSuppInvNo(String suppInvNo) {
        this.suppInvNo = suppInvNo;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
