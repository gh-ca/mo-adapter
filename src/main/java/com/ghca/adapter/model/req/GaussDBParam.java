package com.ghca.adapter.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/3/5 10:27
 */
public class GaussDBParam {

    @NotBlank(message = "AK can not be empty")
    @JsonProperty("AK")
    private String ak;
    @NotBlank(message = "SK can not be empty")
    @JsonProperty("SK")
    private String sk;
    @NotBlank(message = "region can not be empty")
    private String region;
    @NotBlank(message = "resource_space can not be empty")
    private String resource_space;
    @NotBlank(message = "gaussDB can not be empty")
    private String gaussDB;
    @NotBlank(message = "kms_tde_key_id can not be empty")
    private String kms_tde_key_id;
    @NotBlank(message = "kms_project_name can not be empty")
    private String kms_project_name;

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getResource_space() {
        return resource_space;
    }

    public void setResource_space(String resource_space) {
        this.resource_space = resource_space;
    }

    public String getGaussDB() {
        return gaussDB;
    }

    public void setGaussDB(String gaussDB) {
        this.gaussDB = gaussDB;
    }

    public String getKms_tde_key_id() {
        return kms_tde_key_id;
    }

    public void setKms_tde_key_id(String kms_tde_key_id) {
        this.kms_tde_key_id = kms_tde_key_id;
    }

    public String getKms_project_name() {
        return kms_project_name;
    }

    public void setKms_project_name(String kms_project_name) {
        this.kms_project_name = kms_project_name;
    }
}
