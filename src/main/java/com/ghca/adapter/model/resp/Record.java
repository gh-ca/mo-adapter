package com.ghca.adapter.model.resp;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/20 15:50
 */
public class Record {

    private String operation;
    private String result;
    private String rootCause;

    public Record(String operation, String result, String rootCause) {
        this.operation = operation;
        this.result = result;
        this.rootCause = rootCause;
    }

    public String getOperation() {
        return operation;
    }

    public String getResult() {
        return result;
    }

    public String getRootCause() {
        return rootCause;
    }
}
