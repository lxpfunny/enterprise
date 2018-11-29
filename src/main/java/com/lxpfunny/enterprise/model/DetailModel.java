package com.lxpfunny.enterprise.model;

/**
 * Created by lixiaopeng on 2017/7/23.
 */
public class DetailModel {
    private String deliveryDate;//发货日期
    private String receiveDate;//收货日期
    private String typeName;//品名
    private String size;//尺寸规格
    private String pTotal;//总p数
    private String pPlus;//加p数
    private String sum;//数量
    private String price;//单价
    private String cash;//应收金额
    private String customerName;//顾客姓名
    private String property;//工艺属性
    private int type=1;//导入的时候计算单价是否有问题0=有问题，1=没问题
    private String tradeNo;//送货编号
    private String unit;//单位
    private String memo;//备注

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getpTotal() {
        return pTotal;
    }

    public void setpTotal(String pTotal) {
        this.pTotal = pTotal;
    }

    public String getpPlus() {
        return pPlus;
    }

    public void setpPlus(String pPlus) {
        this.pPlus = pPlus;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
