package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.*;
import com.ghca.adapter.utils.FileOperationUtil;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class ResourceSpaceServiceImpl extends BaseService implements ResourceSpaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSpaceServiceImpl.class);

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
        String filePath = FileOperationUtil.getFilePath("group_roles_rel.json");
        String groupRolesRel = FileOperationUtil.getTemplate(filePath);
        ThreadLocal<Map<String, Object>> threadLocal = ThreadUtils.getThreadLocal();
        HashMap<String, Object> map = new HashMap<>();
        map.put("domainId", rsParam.getDomain());
        threadLocal.set(map);
        //查询资源空间
        if (projectService.isExist(rsParam, result)){
            LOGGER.info("Project already exists");
        }else {
            //创建资源空间
            if (!projectService.createProjectInVdc(rsParam, result)){
                return result;
            }
        }
        LOGGER.info("Data after create project: {}", JsonUtils.parseObject2Str(result.getData()));
        //查询企业项目
        if (enterpriseProjectService.isExist(rsParam, result)){
            LOGGER.info("Enterprise project already exists");
        }else {
            //创建企业项目
            if (!enterpriseProjectService.createEnterpriseProjectInProject(rsParam, result)){
                return result;
            }
        }
        LOGGER.info("Data after create enterprise project: {}", JsonUtils.parseObject2Str(result.getData()));
        //创建用户组
        if (!userGroupService.createUserGroup(rsParam, groupRolesRel, result)){
            return result;
        }
        LOGGER.info("Data after create user groups: {}", JsonUtils.parseObject2Str(result.getData()));
        //绑定用户组权限
        if (!userGroupService.bindRoles(rsParam, groupRolesRel, result)){
            return result;
        }
        LOGGER.info("Data after bind roles: {}", JsonUtils.parseObject2Str(result.getData()));
        //创建用户并添加到用户组
        userService.createUserInGroup(rsParam, groupRolesRel, result);
        LOGGER.info("Data after finish: {}", JsonUtils.parseObject2Str(result.getData()));
        LOGGER.info("Create iam finish");
        return result;
    }

}
