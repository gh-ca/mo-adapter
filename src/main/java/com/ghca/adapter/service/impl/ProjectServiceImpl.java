package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.ProjectService;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
public class ProjectServiceImpl extends BaseService implements ProjectService {

    private static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Override
    public boolean createProjectInVdc(RsParam rsParam, Result result) {
        logger.info("start create project");
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("project"));
        Map<String, Object> body = buildBody(rsParam);
        /*
         * body json format
         * {
         * 	"project" : {
         * 		"name" : "xxx",
         * 		"tenant_id" : "xxx",
         * 		"regions" : [{
         * 				"region_id" : "xxx"
         * 			            }
         * 		]
         * 	}
         * }
         */
        ResponseEntity<String> responseEntity = RestUtils.post(url, body, String.class, rsParam.getAk(), rsParam.getSk());
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()){
            logger.error("Create project failed: {}", responseEntity.getBody());
            result.setResult("Failed");
            Record record = new Record();
            record.setOperation("Create project");
            record.setResult("Failed").setRootCause(responseEntity.getBody());
            result.getMessage().add(record);
            return false;
        }
        Map<String, Object> project = (Map<String, Object>) JsonUtils.parseJsonStr2Map(responseEntity.getBody()).get("project");
        String projectId = project.get("id").toString();
        Map<String, Object> data = new HashMap<>();
        data.put("projectId", projectId);
        result.setData(data);
        return true;
    }

    private Map<String, Object> buildBody(RsParam rsParam) {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> project = new HashMap<>();
        List<Map<String, Object>> regions = new ArrayList<>();
        Map<String, Object> region = new HashMap<>();
        String projectName = rsParam.getRegion() + "_" + rsParam.getEnv() + "_" + rsParam.getName() + "_rs_vdc3";
        project.put("name", projectName);
        project.put("tenant_id", rsParam.getVdc());
        region.put("region_id", rsParam.getRegion());
        regions.add(region);
        project.put("regions", regions);
        body.put("project", project);
        return body;
    }

    @Override
    public boolean isExist(RsParam rsParam, Result result){
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(),
            scProperties.getApi().get("projectList").replace("{vdc_id}", rsParam.getVdc()));
        Map<String, Object> query = new HashMap<>();
        String projectName = rsParam.getRegion() + "_" + rsParam.getEnv() + "_" + rsParam.getName() + "_rs_vdc3";
        query.put("name", projectName);
        ResponseEntity<String> responseEntity = RestUtils.get(url, query, String.class, rsParam.getAk(), rsParam.getSk());
        if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()){
            logger.error("Query project: {}", responseEntity.getBody());
            return false;
        }
        List<Map<String, Object>> projects = (List<Map<String, Object>>) JsonUtils.parseJsonStr2Map(responseEntity.getBody()).get("projects");
        for (Map<String, Object> project : projects){
            if (projectName.equals(project.get("name"))){
                String projectId = (String)project.get("id");
                Map<String, Object> data = new HashMap<>();
                data.put("projectId", projectId);
                result.setData(data);
                return true;
            }
        }
        return false;
    }

}
