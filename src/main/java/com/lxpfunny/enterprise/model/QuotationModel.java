package com.lxpfunny.enterprise.model;

/**
 * Created by lixiaopeng on 2017/7/23.
 */
public class QuotationModel {
    private String typeName;//产品名称
    private String size;//照片尺寸
    private String coverPrice;//封面单价
    private String processingFee;//加工费(10P)
    private String pPlusPrice;//换p单价
    private String finishedPrice;//成品单价(10P)
    private String category;//分类

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getCoverPrice() {
        return coverPrice;
    }

    public void setCoverPrice(String coverPrice) {
        this.coverPrice = coverPrice;
    }

    public String getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(String processingFee) {
        this.processingFee = processingFee;
    }

    public String getpPlusPrice() {
        return pPlusPrice;
    }

    public void setpPlusPrice(String pPlusPrice) {
        this.pPlusPrice = pPlusPrice;
    }

    public String getFinishedPrice() {
        return finishedPrice;
    }

    public void setFinishedPrice(String finishedPrice) {
        this.finishedPrice = finishedPrice;
    }
}
