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

    public String getOperation() {
        return operation;
    }

    public Record setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public String getResult() {
        return result;
    }

    public Record setResult(String result) {
        this.result = result;
        return this;
    }

    public String getRootCause() {
        return rootCause;
    }

    public Record setRootCause(String rootCause) {
        this.rootCause = rootCause;
        return this;
    }
}
