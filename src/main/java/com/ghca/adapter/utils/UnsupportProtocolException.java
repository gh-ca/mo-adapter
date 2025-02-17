/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.ghca.adapter.utils;

public class UnsupportProtocolException extends Exception {
    private static final long serialVersionUID = 4312820110480855928L;
    private String msgDes; // 异常对应的描述信息

    public UnsupportProtocolException(String message) {
        super(message);
        msgDes = message;
    }
}
