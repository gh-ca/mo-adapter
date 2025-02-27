package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.ElbParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.ElbService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class ElbServiceImpl extends BaseService implements ElbService {

    private static Logger logger = LoggerFactory.getLogger(ElbServiceImpl.class);

    @Value("${QoS.L4}")
    private String qoSL4;
    @Value("${QoS.L7}")
    private String qoSL7;


    @Override
    public Result createAndBindQoS(ElbParam elbParam, Result result) {
        result.setResult("success");
        ThreadLocal<Map<String, Object>> threadLocal = ThreadUtils.getThreadLocal();
        HashMap<String, Object> map = new HashMap<>();
        map.put("domainId", elbParam.getResource_space());
        threadLocal.set(map);
        String elbDetailUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", "vpc").replace("{region}", elbParam.getRegion()), "",
                cloudProperties.getApi().get("elb").replace("{loadbalancer_id}", elbParam.getElb()));
        ResponseEntity<String> elbDetailResp = RestUtils.get(elbDetailUrl, String.class, elbParam.getAk(), elbParam.getSk());
        if (elbDetailResp == null || !elbDetailResp.getStatusCode().is2xxSuccessful()){
            logger.error("Query elb detail: {}", elbDetailResp.getBody());
            result.setResult("Failed");
            Record record = new Record();
            record.setOperation("Query elb detail");
            record.setResult("Failed").setRootCause(elbDetailResp.getBody());
            result.getMessage().add(record);
            return result;
        }
        String qoSL4Id = "";
        String qoSL7Id = "";
        Map<String, Object> loadbalancer = (Map<String, Object>) JsonUtils.parseJsonStr2Map(elbDetailResp.getBody()).get("loadbalancer");
        //如果符合条件的L4 QoS不存在则创建
        if (loadbalancer.get("l4_flavor_id") != null && StringUtils.isNotBlank(loadbalancer.get("l4_flavor_id").toString())
                && !"null".equals(loadbalancer.get("l4_flavor_id").toString())){
            if (!isExist(elbParam, (String) loadbalancer.get("l4_flavor_id"), result)){
                qoSL4Id = createQos(elbParam, "l4", result);
            }
        }else {
            qoSL4Id = createQos(elbParam, "l4", result);
        }
        //如果符合条件的L7 QoS不存在则创建
        if (loadbalancer.get("l7_flavor_id") != null && StringUtils.isNotBlank(loadbalancer.get("l7_flavor_id").toString())
                && !"null".equals(loadbalancer.get("l7_flavor_id").toString())){
            if (!isExist(elbParam, (String) loadbalancer.get("l7_flavor_id"), result)){
                qoSL7Id = createQos(elbParam, "l7", result);
            }
        }else {
            qoSL7Id = createQos(elbParam, "l7", result);
        }
        bidnQoS(elbParam, qoSL4Id, qoSL7Id, result);
        return result;
    }

    private void bidnQoS(ElbParam elbParam, String qoSL4Id, String qoSL7Id, Result result) {
        if (StringUtils.isNotBlank(qoSL4Id) || StringUtils.isNotBlank(qoSL7Id)) {
            String updateElbUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                            replace("{service}", "vpc").replace("{region}", elbParam.getRegion()), "",
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
            Map<String, Object> loadbalancer = new HashMap<>();
            if (StringUtils.isNotBlank(qoSL4Id)) {
                loadbalancer.put("l4_flavor_id", qoSL4Id);
            }else {
                logger.info("L4 QoS {} already exists", qoSL4Id);
                Record record = new Record();
                record.setOperation("Create L4 QoS");
                record.setResult("Failed").setRootCause("L4 QoS " + qoSL4Id + " already exists");
                result.getMessage().add(record);
            }
            if (StringUtils.isNotBlank(qoSL7Id)) {
                loadbalancer.put("l7_flavor_id", qoSL7Id);
            }else {
                logger.info("L7 QoS {} already exists", qoSL7Id);
                Record record = new Record();
                record.setOperation("Create L7 QoS");
                record.setResult("Failed").setRootCause("L7 QoS " + qoSL7Id + " already exists");
                result.getMessage().add(record);
            }
            body.put("loadbalancer", loadbalancer);
            ResponseEntity<String> updateElbResp = RestUtils.put(updateElbUrl, body, String.class, elbParam.getAk(), elbParam.getSk());
            if (updateElbResp == null || !updateElbResp.getStatusCode().is2xxSuccessful()) {
                logger.error("Bind QoS failed: {}", updateElbResp.getBody());
                result.setResult("Failed");
                Record record = new Record();
                record.setOperation("Bind QoS");
                record.setResult("Failed").setRootCause(updateElbResp.getBody());
                result.getMessage().add(record);
            }
        }
    }

    private String createQos(ElbParam elbParam, String level, Result result) {
        String qoSName = buildQoSName(qoSL4);
        ResponseEntity<String> createL4Resp = createQoS(elbParam, level, qoSName);
        if (createL4Resp == null || !createL4Resp.getStatusCode().is2xxSuccessful()) {
            logger.error("Create {} QoS: {}", level, createL4Resp.getBody());
            result.setResult("Failed");
            Record record = new Record();
            record.setOperation("Create " + level + " QoS");
            record.setResult("Failed").setRootCause(createL4Resp.getBody());
            result.getMessage().add(record);
            return "";
        }
        Map<String, Object> flavor = (Map<String, Object>) JsonUtils.parseJsonStr2Map(createL4Resp.getBody()).get("flavor");
        return flavor.get("id").toString();
    }

    private boolean isExist(ElbParam elbParam, String flavorId, Result result) {
        String queryQoSUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", "vpc").replace("{region}", elbParam.getRegion()), "",
                cloudProperties.getApi().get("queryQoS").replace("{flavor_id}", flavorId));
        ResponseEntity<String> queryQoSResp = RestUtils.get(queryQoSUrl, String.class, elbParam.getAk(), elbParam.getSk());
        if (queryQoSResp == null || !queryQoSResp.getStatusCode().is2xxSuccessful()) {
            logger.error("Query QoS: {}", queryQoSResp.getBody());
            result.setResult("Failed");
            Record record = new Record();
            record.setOperation("Query QoS");
            record.setResult("Failed").setRootCause(queryQoSResp.getBody());
            result.getMessage().add(record);
            return false;
        }
        Map<String, Object> flavor = (Map<String, Object>) JsonUtils.parseJsonStr2Map(queryQoSResp.getBody()).get("flavor");
        String name = (String) flavor.get("name");
        String type = (String) flavor.get("type");
        if ("l4".equals(type)) {
            return name.startsWith(qoSL4);
        }
        if ("l7".equals(type)) {
            return name.startsWith(qoSL7);
        }
        return false;
    }

    private ResponseEntity<String> createQoS(ElbParam elbParam, String level, String flavorName) {
        String createQoSUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", "vpc").replace("{region}", elbParam.getRegion()), "",
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
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 5; i++) {
            int randomInt = secureRandom.nextInt(26);
            int ascii = randomInt + 'a';
            name.append((char) ascii);
        }
        return name.toString();
    }
}
