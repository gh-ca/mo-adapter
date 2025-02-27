package com.ghca.adapter.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 15:27
 */
public class ElbParam {

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
    @NotBlank(message = "vdc can not be empty")
    private String vdc;
    @NotBlank(message = "elb can not be empty")
    private String elb;

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

    public String getVdc() {
        return vdc;
    }

    public void setVdc(String vdc) {
        this.vdc = vdc;
    }

    public String getElb() {
        return elb;
    }

    public void setElb(String elb) {
        this.elb = elb;
    }
}
