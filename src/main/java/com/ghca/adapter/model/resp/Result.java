package com.ghca.adapter.model.resp;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/19 10:51
 */
public class Result {

    private String result;
    private List<Record> message = Lists.newArrayList();
    private Object data;

    public Result() {}

    public Result(String result) {
        this.result = result;
    }

    public Result(String result, Object data) {
        this.result = result;
        this.data = data;
    }

    public String getResult() {
        return result;
    }

    public Result setResult(String result) {
        this.result = result;
        return this;
    }

    public List<Record> getMessage() {
        return message;
    }

    public Result addMessage(String operation, String result, String rootCause) {
        Record record = new Record(operation, result, rootCause);
        this.message.add(record);
        return this;
    }

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }
}
