package com.lxpfunny.enterprise.model;

import java.util.List;
import java.util.Map;

/**
 * Created by lixiaopeng on 2017/7/23.
 */
public class BaseModel {
    private String type;
    private Map<String,List<QuotationModel>> models;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, List<QuotationModel>> getModels() {
        return models;
    }

    public void setModels(Map<String, List<QuotationModel>> models) {
        this.models = models;
    }
}
