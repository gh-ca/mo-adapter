package com.ghca.adapter.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 15:27
 */
public class RsParam {

    @JsonProperty("AK")
    private String ak;
    @JsonProperty("SK")
    private String sk;
    private String region;
    private String vdc;
    private String env;
    private String name;
    private boolean skip_resource_sapce_provision;
    private List<String> cps_admin_aad;
    private List<String> cps_admin_local;
    private List<String> admin_aad;
    private List<String> admin_local;
    private List<String> supp_aad;
    private List<String> supp_local;

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

    public String getVdc() {
        return vdc;
    }

    public void setVdc(String vdc) {
        this.vdc = vdc;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSkip_resource_sapce_provision() {
        return skip_resource_sapce_provision;
    }

    public void setSkip_resource_sapce_provision(boolean skip_resource_sapce_provision) {
        this.skip_resource_sapce_provision = skip_resource_sapce_provision;
    }

    public List<String> getCps_admin_aad() {
        return cps_admin_aad;
    }

    public void setCps_admin_aad(List<String> cps_admin_aad) {
        this.cps_admin_aad = cps_admin_aad;
    }

    public List<String> getCps_admin_local() {
        return cps_admin_local;
    }

    public void setCps_admin_local(List<String> cps_admin_local) {
        this.cps_admin_local = cps_admin_local;
    }

    public List<String> getAdmin_aad() {
        return admin_aad;
    }

    public void setAdmin_aad(List<String> admin_aad) {
        this.admin_aad = admin_aad;
    }

    public List<String> getAdmin_local() {
        return admin_local;
    }

    public void setAdmin_local(List<String> admin_local) {
        this.admin_local = admin_local;
    }

    public List<String> getSupp_aad() {
        return supp_aad;
    }

    public void setSupp_aad(List<String> supp_aad) {
        this.supp_aad = supp_aad;
    }

    public List<String> getSupp_local() {
        return supp_local;
    }

    public void setSupp_local(List<String> supp_local) {
        this.supp_local = supp_local;
    }
}
