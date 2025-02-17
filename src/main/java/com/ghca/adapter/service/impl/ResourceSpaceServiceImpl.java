package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.EnterpriseProjectService;
import com.ghca.adapter.service.ProjectService;
import com.ghca.adapter.service.ResourceSpaceService;
import com.ghca.adapter.service.UserGroupService;
import com.ghca.adapter.service.UserService;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class ResourceSpaceServiceImpl extends BaseService implements ResourceSpaceService {

    private static Logger logger = LoggerFactory.getLogger(ResourceSpaceServiceImpl.class);

    @Resource
    private ProjectService projectService;
    @Resource
    private EnterpriseProjectService enterpriseProjectService;
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private UserService userService;

    @Override
    public String createRs(RsParam rsParam) {
        String errorMessage = null;
        ResponseEntity<String> projectInVdcResponse = projectService.createProjectInVdc(rsParam);
        if (projectInVdcResponse == null || !projectInVdcResponse.getStatusCode().is2xxSuccessful()){
            return "error";
        }
        String projectInVdcResult = projectInVdcResponse.getBody();
        Map<String, Object> project = (Map<String, Object>) JsonUtils.parseJsonStr2Map(projectInVdcResult).get("project");
        String projectId = project.get("id").toString();
        ResponseEntity<String> enterpriseProjectInProjectResponse = enterpriseProjectService.createEnterpriseProjectInProject(
            rsParam, projectId);
        if (enterpriseProjectInProjectResponse == null || !enterpriseProjectInProjectResponse.getStatusCode().is2xxSuccessful()){
            return "error";
        }
        Map<String, Object> enterpriseProject = (Map<String, Object>) JsonUtils.parseJsonStr2Map(enterpriseProjectInProjectResponse.getBody()).get("enterprise_project");
        String enterpriseProjectId = enterpriseProject.get("id").toString();
        List<Map<String, Object>> userGroup = userGroupService.createUserGroupAndAddRoles(rsParam, projectId, enterpriseProjectId);

        String userInGroup = userService.createUserInGroup(rsParam, userGroup, projectId);
        return null;
    }
}
