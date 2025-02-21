package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.UserGroupService;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class UserGroupServiceImpl extends BaseService implements UserGroupService {

    private static Logger logger = LoggerFactory.getLogger(UserGroupServiceImpl.class);

    @Override
    public boolean createUserGroup(RsParam rsParam, String groupRolesRel, Result result) {
        Map<String, Object> existingData = (Map<String, Object>) result.getData();
        String uri = scProperties.getApi().get("userGroup").replace("{vdc_id}", rsParam.getVdc());
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), uri);

        //vdc下所有的用户组列表
        List<Map<String, Object>> userGroups = queryUserGroups(rsParam, url);
        Map<String, String> userGroupsMap = userGroups.stream()
            .collect(Collectors.toMap(map -> (String) map.get("name"), map -> (String) map.get("id"),
                (existingValue, newValue) -> existingValue));
        //新创建的用户组列表
        List<Map<String, Object>> newGroups = new ArrayList<>();
        //已存在的用户组列表
        List<Map<String, Object>> existingGroups = new ArrayList<>();

        Map<Object, Object> groupRolesRelMap = JsonUtils.parseJsonStr2Map(groupRolesRel);
        int count = 0;
        /* 创建3个用户组
         * {env}_cps_{name}_admin_group
         * {env}_{name}_admin_group
         * {env}_{name}_supp_group
         */
        for (Object key : groupRolesRelMap.keySet()){
            String name = ((String) key).replace("{env}", rsParam.getEnv()).replace("{name}", rsParam.getName());
            String userGroupId = userGroupsMap.get(name);
            //如果用户组存在则不创建
            if (StringUtils.isNotEmpty(userGroupId)){
                HashMap<String, Object> userGroup = new HashMap<>();
                userGroup.put(name, userGroupId);
                existingGroups.add(userGroup);
                logger.info("UserGroup {} is already existing", name);
                continue;
            }
            //创建用户组
            ResponseEntity<String> responseEntity = createUserGroup(rsParam, name, url);
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()){
                logger.error("Create userGroup {} failed: {}", name, responseEntity.getBody());
                Record record = new Record();
                record.setOperation("Create userGroup " + name).setResult("Failed").setRootCause(responseEntity.getBody());
                result.setResult("Partial success");
                result.getMessage().add(record);
                continue;
            }
            count++;
            Map<String, Object> groupMap = (Map<String, Object>) JsonUtils.parseJsonStr2Map(responseEntity.getBody()).get("group");
            String groupId = (String) groupMap.get("id");
            Map<String, Object> group = new HashMap<>();
            group.put(name, groupId);
            newGroups.add(group);
        }
        if (count == 0) {
            return false;
        }
        existingData.put("newGroups", newGroups);
        existingData.put("existingGroups", existingGroups);
        return true;
    }

    private static List<Map<String, Object>> queryUserGroups(RsParam rsParam, String url) {
        int start = 0;
        int limit = 100;
        int total = 0;
        int dataTotal;
        Map<String, Object> query = new HashMap<>();
        query.put("start", start);
        query.put("limit", limit);
        List<Map<String, Object>> userGroupsList = new ArrayList<>();
        do {
            ResponseEntity<String> response = RestUtils.get(url, String.class, rsParam.getAk(), rsParam.getSk());
            if (response == null || !response.getStatusCode().is2xxSuccessful()){
                logger.error("Query userGroup failed: {}", response.getBody());
            }
            dataTotal = (int) JsonUtils.parseJsonStr2Map(response.getBody()).get("total");
            userGroupsList.addAll((List<Map<String, Object>>)JsonUtils.parseJsonStr2Map(response.getBody()).get("groups"));
            total += limit;
            start++;
        }while (total < dataTotal);
        return userGroupsList;
    }

    @Override
    public boolean bindRoles(RsParam rsParam, String groupRolesRel, Result result) {
        //查询vdc详情获取domain_id
        ResponseEntity<String> vdcResponseEntity = queryVdcDetail(rsParam);
        if (vdcResponseEntity == null || !vdcResponseEntity.getStatusCode().is2xxSuccessful()){
            logger.error("Query vdcDetail failed", vdcResponseEntity.getBody());
            Record record = new Record();
            record.setOperation("Bind roles : Query vdc detail").setResult("Failed").setRootCause(vdcResponseEntity.getBody());
            result.setResult("partial success");
            result.getMessage().add(record);
            return false;
        }

        Map<String, Object> vdc = (Map<String, Object>) JsonUtils.parseJsonStr2Map(vdcResponseEntity.getBody()).get("vdc");
        String domainId = (String) vdc.get("domain_id");
        HashMap<String, Object> query = new HashMap<>();
        query.put("domain_id", domainId);
        //查询权限列表
        String rolesUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("roles"));
        ResponseEntity<String> rolesResponse = RestUtils.get(rolesUrl, query, String.class, rsParam.getAk(), rsParam.getSk());
        if (rolesResponse == null || !rolesResponse.getStatusCode().is2xxSuccessful()){
            logger.error("Query roles failed", rolesResponse.getBody());
            Record record = new Record();
            record.setOperation("Bind roles : Query roles").setResult("Failed").setRootCause(rolesResponse.getBody());
            result.setResult("Partial success");
            result.getMessage().add(record);
            return false;
        }
        /*
         * role的显示模式，其中：
         * AX表示在domain层显示；
         * XA表示在project层显示；
         * AA表示在domain和project层均显示；
         * XX表示在domain和project层均不显示。
         * find_grained=true就是企业项目支持的
         */
        List<Map<String, Object>> sysRolesList = (List<Map<String, Object>>) JsonUtils.parseJsonStr2Map(rolesResponse.getBody()).get("system_roles");
        List<Map<String, Object>> cusRolesList = (List<Map<String, Object>>) JsonUtils.parseJsonStr2Map(rolesResponse.getBody()).get("custom_roles");
        List<Map<String, Object>> rolesList = Stream.of(sysRolesList, cusRolesList)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Map<String, String> vdcRoles = rolesList.stream()
            .filter(map -> map.get("display_name") != null && StringUtils.isNotBlank(map.get("display_name").toString()) && !"null".equals(map.get("display_name").toString())
                && ("AX".equals(map.get("type")) || "AA".equals(map.get("type"))))
            .collect(Collectors.toMap(map -> (String)map.get("display_name"), map -> (String)map.get("id"), (existingValue, newValue) -> existingValue));

        Map<String, String> projectRoles = rolesList.stream()
            .filter(map -> map.get("display_name") != null && StringUtils.isNotBlank(map.get("display_name").toString()) && !"null".equals(map.get("display_name").toString())
                && ("XA".equals(map.get("type")) || "AA".equals(map.get("type"))))
            .collect(Collectors.toMap(map -> (String)map.get("display_name"), map -> (String)map.get("id"), (existingValue, newValue) -> existingValue));

        Map<String, String> enterpriseProjectRoles = rolesList.stream()
            .filter(map -> map.get("display_name") != null && StringUtils.isNotBlank(map.get("display_name").toString()) && !"null".equals(map.get("display_name").toString())
                && "fine_grained".equals(map.get("flag")))
            .collect(Collectors.toMap(map -> (String)map.get("display_name"), map -> (String)map.get("id"), (existingValue, newValue) -> existingValue));

        Map<Object, Object> groupRolesRelMap = JsonUtils.parseJsonStr2Map(groupRolesRel);
        Map<String, Object> existingData = (Map<String, Object>) result.getData();
        String projectId = (String) existingData.get("projectId");
        String enterpriseProjectId = (String) existingData.get("enterpriseProjectId");
        List<Map<String, Object>> newGroups = (List<Map<String, Object>>) existingData.get("newGroups");
        for (Map<String, Object> group : newGroups){
            for (String key : group.keySet()){
                Map<String, Object> map = new HashMap<>();
                for (Object groupRolesRelKey : groupRolesRelMap.keySet()){
                    String replace = ((String) groupRolesRelKey).replace("{env}", rsParam.getEnv())
                        .replace("{name}", rsParam.getName());
                    if (key.equals(replace)){
                        map = (Map<String, Object>) groupRolesRelMap.get(groupRolesRelKey);
                        break;
                    }
                }
                //配置中的权限列表
                List<String> global = (List<String>) map.get("global");
                List<String> resourceSpace = (List<String>) map.get("resourceSpace");
                List<String> enterpriseProjects = (List<String>) map.get("enterpriseProjects");
                //过滤后需要绑定的权限列表
                List<String> globalIds = new ArrayList<>();
                List<String> resourceSpaceIds = new ArrayList<>();
                List<String> enterpriseProjectsIds = new ArrayList<>();
                List<String> globalIdsByName = new ArrayList<>();
                List<String> resourceSpaceIdsByName = new ArrayList<>();
                List<String> enterpriseProjectsIdsByName = new ArrayList<>();
                //MO中不存在的权限列表
                List<String> notExistGlobal = new ArrayList<>();
                List<String> notExistResourceSpace = new ArrayList<>();
                List<String> notExistEnterpriseProjectsIds = new ArrayList<>();
                //组装好的权限列表
                List<Map<String, Object>> addRoles = new ArrayList<>();

                //过滤不存在的权限列表
                for (String role : global){
                    if (vdcRoles.get(role) != null){
                        globalIds.add(vdcRoles.get(role));
                        globalIdsByName.add(role);
                    }else {
                        notExistGlobal.add(role);
                    }
                }
                for (String role : resourceSpace){
                    if (projectRoles.get(role) != null){
                        resourceSpaceIds.add(projectRoles.get(role));
                        resourceSpaceIdsByName.add(role);
                    }else {
                        notExistResourceSpace.add(role);
                    }
                }
                for (String role : enterpriseProjects){
                    if (enterpriseProjectRoles.get(role) != null){
                        enterpriseProjectsIds.add(enterpriseProjectRoles.get(role));
                        enterpriseProjectsIdsByName.add(role);
                    }else {
                        notExistEnterpriseProjectsIds.add(role);
                    }
                }

                //组装vdc权限targets
                for (String id : globalIds){
                    Map<String, Object> role = new HashMap<>();
                    List<Map<String, String>> targets = new ArrayList<>();
                    Map<String, String> target = new HashMap<>();
                    target.put("id", domainId);
                    role.put("id", id);
                    if (resourceSpaceIds.contains(id)){
                        Map<String, String> addTarget = new HashMap<>();
                        addTarget.put("id", projectId);
                        targets.add(addTarget);
                        resourceSpaceIds.remove(id);
                    }
                    if (enterpriseProjectsIds.contains(id)){
                        Map<String, String> addTarget = new HashMap<>();
                        addTarget.put("id", enterpriseProjectId);
                        targets.add(addTarget);
                        enterpriseProjectsIds.remove(id);
                    }
                    targets.add(target);
                    role.put("targets", targets);
                    addRoles.add(role);
                }
                //组装资源空间权限targets
                for (String id : resourceSpaceIds){
                    Map<String, Object> role = new HashMap<>();
                    List<Map<String, String>> targets = new ArrayList<>();
                    Map<String, String> target = new HashMap<>();
                    target.put("id", projectId);
                    role.put("id", id);
                    if (enterpriseProjectsIds.contains(id)){
                        Map<String, String> addTarget = new HashMap<>();
                        addTarget.put("id", enterpriseProjectId);
                        targets.add(addTarget);
                        enterpriseProjectsIds.remove(id);
                    }
                    targets.add(target);
                    role.put("targets", targets);
                    addRoles.add(role);
                }

                logger.info("\n{}用户组：\n vdc要绑定的权限在MO中不存在的为 {}\n 资源空间要绑定的权限在MO中不存在的为 {}\n 企业项目要绑定的权限在MO中不存在的为 {}",
                    key, notExistGlobal, notExistResourceSpace, notExistEnterpriseProjectsIds);


                String groupRolesUri = scProperties.getApi().get("groupRoles").replace("{group_id}", (String)group.get(key));
                String groupRolesUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), groupRolesUri);
                //组装body
                Map<String, Object> body = new HashMap<>();
                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put("action", "add");
                groupMap.put("roles", addRoles);
                body.put("group", groupMap);
                //绑定vdc、资源空间权限列表
                ResponseEntity<String> addRolesResponse = RestUtils.put(groupRolesUrl, body, String.class, rsParam.getAk(), rsParam.getSk());
                if (addRolesResponse == null || !addRolesResponse.getStatusCode().is2xxSuccessful()){
                    logger.error("Bind vdc and project roles failed", addRolesResponse.getBody());
                    Record record = new Record();
                    record.setOperation("Bind roles : " + key + "Bind vdc roles " + JsonUtils.parseObject2Str(globalIdsByName)
                        + " and project roles " + JsonUtils.parseObject2Str(resourceSpaceIdsByName)).setResult("Failed").setRootCause(addRolesResponse.getBody());
                    result.setResult("Partial success");
                    result.getMessage().add(record);
                }

                //绑定企业项目权限列表
                for (int i = 0; i < enterpriseProjectsIds.size(); i++){
                    String enterpriseGroupRolesUri = scProperties.getApi().get("enterpriseGroupRoles").replace("{enterprise_project_id}", enterpriseProjectId).
                        replace("{group_id}", (String)group.get(key)).replace("{role_id}", enterpriseProjectsIds.get(i));
                    String enterpriseGroupRolesUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), enterpriseGroupRolesUri);
                    ResponseEntity<String> addEnterpriseGroupRolesResponse = RestUtils.put(enterpriseGroupRolesUrl, null, String.class, rsParam.getAk(), rsParam.getSk());
                    if (addEnterpriseGroupRolesResponse == null || !addEnterpriseGroupRolesResponse.getStatusCode().is2xxSuccessful()){
                        logger.error("Bind enterprise project roles failed", addEnterpriseGroupRolesResponse.getBody());
                        Record record = new Record();
                        record.setOperation("Bind roles : " + key + "bind enterprise project role " + enterpriseProjectsIdsByName.get(i)).setResult("Failed").setRootCause(addEnterpriseGroupRolesResponse.getBody());
                        result.setResult("Partial success");
                        result.getMessage().add(record);
                    }
                }
            }
        }
        return true;
    }


    private ResponseEntity<String> queryVdcDetail(RsParam rsParam){
        String uri = scProperties.getApi().get("vdcDetail").replace("{vdc_id}", rsParam.getVdc());
        String url = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), uri);
        ResponseEntity<String> response = RestUtils.get(url, String.class, rsParam.getAk(), rsParam.getSk());
        return response;
    }

    private ResponseEntity<String> createUserGroup(RsParam rsParam, String name, String url) {
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> group = new HashMap<>();
        group.put("name", name);
        body.put("group", group);
        /*
         * body json format
         * {
         * 	"group" : {
         * 		"name" : "xxx"
         * 	    }
         * }
         */
        ResponseEntity<String> responseEntity = RestUtils.post(url, body, String.class, rsParam.getAk(), rsParam.getSk());
        return responseEntity;
    }
}
