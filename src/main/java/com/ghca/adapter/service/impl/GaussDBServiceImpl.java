package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.GaussDBParam;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.GaussDBService;
import com.ghca.adapter.utils.Constant;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/3/5 10:49
 */
@Service
public class GaussDBServiceImpl extends BaseService implements GaussDBService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GaussDBServiceImpl.class);

    @Value("${job.timeout}")
    public int timeout;


    @Override
    public Result turnOnTde(GaussDBParam gaussDBParam, Result result) {
        String tdeUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", Constant.GAUSSDB).replace("{region}", gaussDBParam.getRegion()), "",
                cloudProperties.getApi().get("gaussDb").replace("{project_id}", gaussDBParam.getResource_space())
                        .replace("{instance_id}", gaussDBParam.getGaussDB()));
        Map<Object, Object> body = Maps.newHashMap();
        body.put("kms_tde_key_id", gaussDBParam.getKms_tde_key_id());
        body.put("kms_tde_status", "on");
        body.put("kms_project_name", gaussDBParam.getKms_project_name());
        ResponseEntity<String> tdeResp = RestUtils.put(tdeUrl, body, String.class, gaussDBParam.getAk(), gaussDBParam.getSk());
        if (!tdeResp.getStatusCode().is2xxSuccessful()){
            LOGGER.error("Create a job to turn on tde : {}", tdeResp.getBody());
            return result.addMessage("Create a job to turn on tde", Constant.FAILED, tdeResp.getBody()).setResult(Constant.FAILED);
        }
        Map<String, Object> resp = JsonUtils.parseJsonStr2Map(tdeResp.getBody());
        String jobId = (String) resp.get("job_id");
        String jobUrl = RestUtils.buildUrl(cloudProperties.getScheme(), cloudProperties.getHost().
                        replace("{service}", Constant.GAUSSDB).replace("{region}", gaussDBParam.getRegion()), "",
                cloudProperties.getApi().get("gaussDbJob").replace("{project_id}", gaussDBParam.getResource_space())
                        .replace("{id}", jobId));
        long expirationTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.MINUTES);
        while (System.currentTimeMillis() < expirationTime){
            ResponseEntity<String> jobResp = RestUtils.get(jobUrl, String.class, gaussDBParam.getAk(), gaussDBParam.getSk());
            if (!jobResp.getStatusCode().is2xxSuccessful()){
                LOGGER.error("Turn on tde: {}", jobResp.getBody());
                return result.addMessage("Turn on tde", Constant.FAILED, jobResp.getBody()).setResult(Constant.FAILED);
            }
            Map<String, Object> jobMap = JsonUtils.parseJsonStr2Map(jobResp.getBody());
            Map<String, Object> job = (Map<String, Object>) jobMap.get("job");
            String status = (String) job.get("status");
            if (Constant.RUNNING.equals(status)){
                LOGGER.info("Job {} is running: {}", jobId, job.get("progress"));
                continue;
            }
            if (Constant.FAILED.equals(status)){
                LOGGER.error("Job {} is failed: {}", jobId, job.get("fail_reason"));
                return result.addMessage("Turn on tde", Constant.FAILED, (String) job.get("fail_reason")).setResult(Constant.FAILED);
            }
            if (Constant.COMPLETED.equals(status)){
                return result;
            }
        }
        LOGGER.error("Job {} timeout", jobId);
        result.addMessage("Turn on tde", Constant.FAILED, "Job " + jobId + " timeout").setResult(Constant.FAILED);
        return result;
    }
}
