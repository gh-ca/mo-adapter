package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.ProjectService;
import com.ghca.adapter.utils.RestUtils;

import org.jetbrains.annotations.NotNull;
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
    public ResponseEntity<String> createProjectInVdc(RsParam rsParam) {
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
        return responseEntity;
    }

    @NotNull
    private static Map<String, Object> buildBody(RsParam rsParam) {
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
}
