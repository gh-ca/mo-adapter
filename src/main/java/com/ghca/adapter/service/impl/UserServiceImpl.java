package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.UserService;
import com.ghca.adapter.utils.FileOperationUtil;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
@Service
public class UserServiceImpl extends BaseService implements UserService {

    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${user.pwd}")
    private String pwd;
    @Value("${user.saml.env.plike}")
    private String plikeSuffix;
    @Value("${user.saml.env.prod}")
    private String prodSuffix;

    @Override
    public String createUserInGroup(RsParam rsParam, List<Map<String, Object>> groups, String projectId) {
        String filePath = FileOperationUtil.getFilePath("group_roles_rel.json");
        String groupRolesRel = FileOperationUtil.getTemplate(filePath);
        Map<Object, Object> groupRolesRelMap = JsonUtils.parseJsonStr2Map(groupRolesRel);
        String userUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("user").replace("{vdc_id}", rsParam.getVdc()));
        for (Map<String, Object> group : groups){
            for (String key : group.keySet()){
                Map<String, Object> groupMap = (Map<String, Object>) groupRolesRelMap.get(key);
                List<Map<String, Object>> variableFields = (List<Map<String, Object>>) groupMap.get("variableFields");
                for (Map<String, Object> fields : variableFields){
                    for (String field : fields.keySet()){
                        Class<?> rsParamCls = RsParam.class;
                        try {
                            Method method = rsParamCls.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
                            List<String> userNames = (List<String>) method.invoke(rsParam);
                            for (String userName : userNames){
                                Map<String, Object> body = new HashMap<>();
                                Map<String, Object> user = new HashMap<>();
                                user.put("password", pwd);
                                String authType = "0";
                                if ("saml".equals(fields.get(field).toString())){
                                    authType = "1";
                                    if ("plike".equals(rsParam.getEnv())){
                                        userName += plikeSuffix;
                                    }else if ("prod".equals(rsParam.getEnv()))
                                        userName += prodSuffix;
                                }
                                user.put("auth_type", authType);
                                user.put("name", userName);
                                body.put("user", user);
                                ResponseEntity<String> userResponse = RestUtils.post(userUrl, body, String.class, rsParam.getAk(), rsParam.getSk());
                                if (userResponse == null || !userResponse.getStatusCode().is2xxSuccessful()){
                                    logger.error("create user {} fail : {}", userName, userResponse.getBody());
                                    continue;
                                }
                                Map<String, Object> userMap = (Map<String, Object>) JsonUtils.parseJsonStr2Map(userResponse.getBody()).get("user");
                                String addUserToGroupUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("addUserToGroup").
                                    replace("{group_id}", group.get(key).toString()).replace("{user_id}", userMap.get("id").toString()));
                                ResponseEntity<String> addUserToGroupResponse = RestUtils.put(addUserToGroupUrl, null, String.class, rsParam.getAk(), rsParam.getSk());
                            }
                        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
                            logger.error("invoke users method error", e);
                        }
                    }
                }
            }
        }
        return null;
    }
}
