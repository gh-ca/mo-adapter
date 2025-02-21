package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.EnterpriseProjectService;
import com.ghca.adapter.service.ProjectService;
import com.ghca.adapter.service.ResourceSpaceService;
import com.ghca.adapter.service.UserGroupService;
import com.ghca.adapter.service.UserService;
import com.ghca.adapter.utils.FileOperationUtil;
import com.ghca.adapter.utils.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
    public Result createRs(RsParam rsParam, Result result) {
        result.setResult("success");
        String filePath = FileOperationUtil.getFilePath("group_roles_rel.json");
        String groupRolesRel = FileOperationUtil.getTemplate(filePath);
        //查询资源空间
        if (projectService.isExist(rsParam, result)){
            logger.info("Project is already exist");
        }
        //创建资源空间
        if (!projectService.createProjectInVdc(rsParam, result)){
            return result;
        }
        logger.info("Data after create project: {}", JsonUtils.parseObject2Str(result.getData()));
        //查询企业项目
        if (enterpriseProjectService.isExist(rsParam, result)){
            logger.info("Enterprise project is already exist");
        }
        //创建企业项目
        if (!enterpriseProjectService.createEnterpriseProjectInProject(rsParam, result)){
            return result;
        }
        logger.info("Data after create enterprise project: {}", JsonUtils.parseObject2Str(result.getData()));
        //创建用户组
        if (!userGroupService.createUserGroup(rsParam, groupRolesRel, result)){
            return result;
        }
        logger.info("Data after create userGroup: {}", JsonUtils.parseObject2Str(result.getData()));
        //绑定用户组权限
        if (!userGroupService.bindRoles(rsParam, groupRolesRel, result)){
            return result;
        }
        logger.info("Data after bind roles: {}", JsonUtils.parseObject2Str(result.getData()));
        //创建用户并添加到用户组
        userService.createUserInGroup(rsParam, groupRolesRel, result);
        logger.info("Data after finish: {}", JsonUtils.parseObject2Str(result.getData()));
        logger.info("Create finish");
        return result;
    }

}
