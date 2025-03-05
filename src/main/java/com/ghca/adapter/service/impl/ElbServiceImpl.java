package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.ElbParam;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.ElbService;
import com.ghca.adapter.utils.Constant;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;
import com.ghca.adapter.utils.ThreadUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class ElbServiceImpl extends BaseService implements ElbService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElbServiceImpl.class);

    @Value("${QoS.L4}")
    private String qoSL4;
    @Value("${QoS.L7}")
    private String qoSL7;


    @Override
    public Result createAndBindQoS(ElbParam elbParam, Result result) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> qoSs = new ArrayList<>();
        data.put("bind", qoSs);
        result.setData(data);
        ThreadLocal<Map<String, Object>> threadLocal = ThreadUtils.getThreadLocal();
        HashMap<String, Object> map = new HashMap<>();
        map.put("projectId", elbParam.getResource_space());
        threadLocal.set(map);
        String elbDetailUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", Constant.VPC).replace("{region}", elbParam.getRegion()), "",
                cloudProperties.getApi().get("elb").replace("{loadbalancer_id}", elbParam.getElb()));
        ResponseEntity<String> elbDetailResp = RestUtils.get(elbDetailUrl, String.class, elbParam.getAk(), elbParam.getSk());
        if (!elbDetailResp.getStatusCode().is2xxSuccessful()){
            LOGGER.error("Query elb detail: {}", elbDetailResp.getBody());
            return result.addMessage("Query elb detail", Constant.FAILED, elbDetailResp.getBody()).setResult(Constant.FAILED);
        }
        String qoSL4Id = "";
        String qoSL7Id = "";
        Map<String, Object> loadbalancer = (Map<String, Object>) JsonUtils.parseJsonStr2Map(elbDetailResp.getBody()).get("loadbalancer");
        //如果符合条件的L4 QoS不存在则创建
        if (loadbalancer.get(Constant.L_4_FLAVOR_ID) != null && StringUtils.isNotBlank(loadbalancer.get(Constant.L_4_FLAVOR_ID).toString())
                && !"null".equals(loadbalancer.get(Constant.L_4_FLAVOR_ID).toString())){
            if (!isExist(elbParam, (String) loadbalancer.get(Constant.L_4_FLAVOR_ID), result)){
                qoSL4Id = createQos(elbParam, Constant.L4, result);
            }
        }else {
            qoSL4Id = createQos(elbParam, Constant.L4, result);
        }
        //如果符合条件的L7 QoS不存在则创建
        if (loadbalancer.get(Constant.L_7_FLAVOR_ID) != null && StringUtils.isNotBlank(loadbalancer.get(Constant.L_7_FLAVOR_ID).toString())
                && !"null".equals(loadbalancer.get(Constant.L_7_FLAVOR_ID).toString())){
            if (!isExist(elbParam, (String) loadbalancer.get(Constant.L_7_FLAVOR_ID), result)){
                qoSL7Id = createQos(elbParam, Constant.L7, result);
            }
        }else {
            qoSL7Id = createQos(elbParam, Constant.L7, result);
        }
        bindQoS(elbParam, qoSL4Id, qoSL7Id, result);
        return result;
    }

    private void bindQoS(ElbParam elbParam, String qoSL4Id, String qoSL7Id, Result result) {
        if (StringUtils.isNotBlank(qoSL4Id) || StringUtils.isNotBlank(qoSL7Id)) {
            String updateElbUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                            replace("{service}", Constant.VPC).replace("{region}", elbParam.getRegion()), "",
                    cloudProperties.getApi().get("elb").replace("{loadbalancer_id}", elbParam.getElb()));
            /*
             * {
             * 	"loadbalancer" : {
             * 		"l4_flavor_id" : "xxx",
             * 		"l7_flavor_id" : "xxx"
             * 	    }
             * }
             */
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> loadBalancer = new HashMap<>();
            List<Object> qoSs = new ArrayList<>();
            if (StringUtils.isNotBlank(qoSL4Id)) {
                loadBalancer.put(Constant.L_4_FLAVOR_ID, qoSL4Id);
                Map<String, Object> qoS = new HashMap<>();
                qoS.put("l4_qos_id", qoSL4Id);
                qoSs.add(qoS);
            }else {
                LOGGER.info("L4 QoS already exists");
                result.addMessage("Bind L4 QoS",Constant.FAILED, "L4 QoS already exists");
            }
            if (StringUtils.isNotBlank(qoSL7Id)) {
                loadBalancer.put(Constant.L_7_FLAVOR_ID, qoSL7Id);
                Map<String, Object> qoS = new HashMap<>();
                qoS.put("l7_qos_id", qoSL7Id);
                qoSs.add(qoS);
            }else {
                LOGGER.info("L7 QoS already exists");
                result.addMessage("Bind L7 QoS", Constant.FAILED, "L7 QoS already exists");
            }
            body.put("loadbalancer", loadBalancer);
            ResponseEntity<String> updateElbResp = RestUtils.put(updateElbUrl, body, String.class, elbParam.getAk(), elbParam.getSk());
            if (!updateElbResp.getStatusCode().is2xxSuccessful()) {
                LOGGER.error("Bind QoS failed: {}", updateElbResp.getBody());
                result.addMessage("Bind QoS", Constant.FAILED, updateElbResp.getBody()).setResult(Constant.FAILED);
            }
            Map<String, Object> data = (Map<String, Object>) result.getData();
            data.put("bind", qoSs);
            result.setData(data);
        }else {
            LOGGER.info("L4 and L7 QoS already exists");
            result.addMessage("Bind L4 QoS", Constant.FAILED, "L4 QoS already exists");
            result.addMessage("Bind L7 QoS", Constant.FAILED, "L7 QoS already exists");
        }
    }

    private String createQos(ElbParam elbParam, String level, Result result) {
        String prefix = "";
        if (Constant.L4.equals(level)) {
            prefix = qoSL4;
        }
        if (Constant.L7.equals(level)) {
            prefix = qoSL7;
        }
        String qoSName = buildQoSName(prefix);
        ResponseEntity<String> createL4Resp = createQoS(elbParam, level, qoSName);
        if (!createL4Resp.getStatusCode().is2xxSuccessful()) {
            LOGGER.error("Create {} QoS: {}", level, createL4Resp.getBody());
            result.addMessage("Create " + level + " QoS", Constant.FAILED, createL4Resp.getBody()).setResult(Constant.FAILED);
            return "";
        }
        Map<String, Object> flavor = (Map<String, Object>) JsonUtils.parseJsonStr2Map(createL4Resp.getBody()).get("flavor");
        return flavor.get("id").toString();
    }

    private boolean isExist(ElbParam elbParam, String flavorId, Result result) {
        String queryQoSUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", Constant.VPC).replace("{region}", elbParam.getRegion()), "",
                cloudProperties.getApi().get("queryQoS").replace("{flavor_id}", flavorId));
        ResponseEntity<String> queryQoSResp = RestUtils.get(queryQoSUrl, String.class, elbParam.getAk(), elbParam.getSk());
        if (!queryQoSResp.getStatusCode().is2xxSuccessful()) {
            LOGGER.error("Query QoS: {}", queryQoSResp.getBody());
            result.addMessage("Query QoS", Constant.FAILED, queryQoSResp.getBody()).setResult(Constant.FAILED);
            return false;
        }
        Map<String, Object> flavor = (Map<String, Object>) JsonUtils.parseJsonStr2Map(queryQoSResp.getBody()).get("flavor");
        String name = (String) flavor.get("name");
        String type = (String) flavor.get("type");
        if (Constant.L4.equals(type)) {
            return name.startsWith(qoSL4);
        }
        if (Constant.L7.equals(type)) {
            return name.startsWith(qoSL7);
        }
        return false;
    }

    private ResponseEntity<String> createQoS(ElbParam elbParam, String level, String flavorName) {
        String createQoSUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", Constant.VPC).replace("{region}", elbParam.getRegion()), "",
                cloudProperties.getApi().get("qoS"));
        /*
         * {
         * 	"flavor" : {
         * 		"name" : "xxx",
         * 		"project_id" : "xxx",
         * 		"type" : "l4",
         * 		"info" : []
         * 	    }
         * }
         */
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> flavor = new HashMap<>();
        flavor.put("name", flavorName);
        flavor.put("project_id", elbParam.getResource_space());
        flavor.put("type", level);
        flavor.put("info", Lists.newArrayList());
        body.put("flavor", flavor);
        return RestUtils.post(createQoSUrl, body, String.class, elbParam.getAk(), elbParam.getSk());
    }

    private String buildQoSName(String prefix) {
        StringBuilder name = new StringBuilder(prefix);
        name.append(System.currentTimeMillis());
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 5; i++) {
            name.append(secureRandom.nextInt(10));
        }
        return name.toString();
    }
}
