package com.lxpfunny.enterprise.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lixiaopeng on 2017/8/1.
 */
public class TotalModel {
    private String deliveryDate;//发货日期
    private String typeName;//品名
    private String size;//尺寸规格
    private String property;//工艺属性
    private String price;//单价
    private String pPlus;//加p数
    private Map<Integer, Integer> sum = new HashMap<>();

    public Map<Integer, Integer> getSum() {
        return sum;
    }

    public void setSum(Map<Integer, Integer> sum) {
        this.sum = sum;
    }

    public String getpPlus() {
        return pPlus;
    }

    public void setpPlus(String pPlus) {
        this.pPlus = pPlus;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
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

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
