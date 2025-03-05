/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.ghca.adapter.utils;

public final class Constant {
    // verify ssl certificate (true) or do not verify (false)
    public static final boolean DO_VERIFY = false;

    public static final String HTTPS = "HTTPS";
    public static final String TRUST_MANAGER_FACTORY = "SunX509";
    public static final String GM_PROTOCOL = "GMTLS";
    public static final String INTERNATIONAL_PROTOCOL = "TLSv1.2";
    public static final String SIGNATURE_ALGORITHM_SDK_HMAC_SHA256 = "SDK-HMAC-SHA256";
    public static final String SIGNATURE_ALGORITHM_SDK_HMAC_SM3 = "SDK-HMAC-SM3";
    public static final String[] SUPPORTED_CIPHER_SUITES = {"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"};
    public static final String SECURE_RANDOM_ALGORITHM_NATIVE_PRNG_NON_BLOCKING = "NativePRNGNonBlocking";
    public static final String L_4_FLAVOR_ID = "l4_flavor_id";
    public static final String L_7_FLAVOR_ID = "l7_flavor_id";
    public static final String VPC = "vpc";
    public static final String SAML = "saml";
    public static final String PLIKE = "plike";
    public static final String PROD = "prod";
    public static final String L4 = "l4";
    public static final String L7 = "l7";
    public static final String API = "api";
    public static final String GAUSSDB = "gaussdb";
    public static final String RUNNING = "Running";
    public static final String FAILED = "Failed";
    public static final String COMPLETED = "Completed";

    private Constant() {
    }
}