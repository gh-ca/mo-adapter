package com.ghca.adapter.model.resp;

import java.util.List;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/19 10:51
 */
public class Result {

    private String result;
    private List<Record> message;
    private Object data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Record> getMessage() {
        return message;
    }

    public void setMessage(List<Record> message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
