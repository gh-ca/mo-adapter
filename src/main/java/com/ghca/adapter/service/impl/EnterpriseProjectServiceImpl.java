package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.EnterpriseProjectService;
import com.ghca.adapter.utils.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    public ResponseEntity<String> createEnterpriseProjectInProject(RsParam rsParam, String projectId) {
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("enterpriseProject"));
        Map<String, Object> body = new HashMap<>();
        String enterpriseProjectName = rsParam.getEnv() + "_" + rsParam.getName() + "_eps_vdc3";
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
        return responseEntity;
    }
}
