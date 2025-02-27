package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.EnterpriseProjectService;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class EnterpriseProjectServiceImpl extends BaseService implements EnterpriseProjectService {

    private static Logger logger = LoggerFactory.getLogger(EnterpriseProjectServiceImpl.class);

    @Override
    public boolean createEnterpriseProjectInProject(RsParam rsParam, Result result) {
        logger.info("Start create enterprise project");
        Map<String, Object> existingData = (Map<String, Object>) result.getData();
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("enterpriseProject"));
        Map<String, Object> body = new HashMap<>();
        String enterpriseProjectName = rsParam.getEnv() + "_" + rsParam.getName() + "_eps_vdc3";
        String projectId = (String)existingData.get("projectId");
        body.put("project_id", projectId);
        body.put("name", enterpriseProjectName);
        /*
         * body json format
         * {
         * 	"project_id": "",
         * 	"name" : ""
         * }
         */
        ResponseEntity<String> responseEntity = RestUtils.post(url, body, String.class, rsParam.getAk(), rsParam.getSk());
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()){
            logger.error("Create enterprise project failed: {}", responseEntity.getBody());
            result.setResult("Partial success");
            Record record = new Record();
            record.setOperation("Create enterprise project");
            record.setResult("Failed").setRootCause(responseEntity.getBody());
            result.getMessage().add(record);
            return false;
        }
        Map<String, Object> enterpriseProject = (Map<String, Object>) JsonUtils.parseJsonStr2Map(responseEntity.getBody()).get("enterprise_project");
        String enterpriseProjectId = enterpriseProject.get("id").toString();
        existingData.put("enterpriseProjectId", enterpriseProjectId);
        return true;
    }

    @Override
    public boolean isExist(RsParam rsParam, Result result){
        Map<String, Object> existingData = (Map<String, Object>) result.getData();
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("enterpriseProject"));
        String enterpriseProjectName = rsParam.getEnv() + "_" + rsParam.getName() + "_eps_vdc3";
        String projectId = (String)existingData.get("projectId");
        Map<String, Object> query = new HashMap<>();
        query.put("query_type", "list");
        query.put("project_id", projectId);
        query.put("name", enterpriseProjectName);
        ResponseEntity<String> responseEntity = RestUtils.get(url, query, String.class, rsParam.getAk(), rsParam.getSk());
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()){
            logger.error("Query enterprise project failed: {}", responseEntity.getBody());
            return false;
        }
        List<Map<String, Object>> enterpriseProjects = (List<Map<String, Object>>) JsonUtils.parseJsonStr2Map(responseEntity.getBody()).get("enterprise_projects");
        for (Map<String, Object> enterpriseProject : enterpriseProjects){
            if (enterpriseProjectName.equals(enterpriseProject.get("name"))){
                String enterpriseProjectId = (String)enterpriseProject.get("id");
                Map<String, Object> data = (Map<String, Object>) result.getData();
                data.put("enterpriseProjectId", enterpriseProjectId);
                result.setData(data);
                return true;
            }
        }
        return false;
    }
}
