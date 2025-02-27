package com.ghca.adapter.service.impl;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.BaseService;
import com.ghca.adapter.service.UserService;
import com.ghca.adapter.utils.FileOperationUtil;
import com.ghca.adapter.utils.JsonUtils;
import com.ghca.adapter.utils.RestUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public boolean createUserInGroup(RsParam rsParam, String groupRolesRel, Result result) {
        logger.info("Start create user");
        String userUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("user").replace("{vdc_id}", rsParam.getVdc()));
        Map<Object, Object> groupRolesRelMap = JsonUtils.parseJsonStr2Map(groupRolesRel);
        Map<String, Object> existingData = (Map<String, Object>) result.getData();
        List<Map<String, Object>> newGroups = (List<Map<String, Object>>) existingData.get("newGroups");
        List<Map<String, Object>> existingGroups = (List<Map<String, Object>>) existingData.get("existingGroups");
        List<Map<String, Object>> allGroups = Stream.of(newGroups, existingGroups).flatMap(Collection::stream).collect(Collectors.toList());
        //vdc下所有用户列表
        List<Map<String, Object>> users = queryUsers(rsParam, userUrl);
        Map<String, String> usersMap = users.stream()
            .collect(Collectors.toMap(map -> (String) map.get("name"), map -> (String) map.get("id"),
                (existingValue, newValue) -> existingValue));

        for (Map<String, Object> group : allGroups){
            for (String key : group.keySet()){
                Map<String, Object> groupMap = new HashMap<>();
                for (Object groupRolesRelKey : groupRolesRelMap.keySet()){
                    String replace = ((String) groupRolesRelKey).replace("{env}", rsParam.getEnv())
                        .replace("{name}", rsParam.getName());
                    if (key.equals(replace)){
                        groupMap = (Map<String, Object>) groupRolesRelMap.get(groupRolesRelKey);
                        break;
                    }
                }
                List<Map<String, Object>> variableFields = (List<Map<String, Object>>) groupMap.get("variableFields");
                for (Map<String, Object> fields : variableFields){
                    for (String field : fields.keySet()){
                        Class<?> rsParamCls = RsParam.class;
                        try {
                            Method method = rsParamCls.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
                            List<String> userNames = (List<String>) method.invoke(rsParam);
                            for (String userName : userNames){
                                String authType = "0";
                                if ("saml".equals(fields.get(field).toString())){
                                    authType = "1";
                                    if ("plike".equals(rsParam.getEnv())){
                                        userName += plikeSuffix;
                                    }else if ("prod".equals(rsParam.getEnv()))
                                        userName += prodSuffix;
                                }
                                String userId = usersMap.get(userName);
                                //如果用户不存在则创建
                                if (StringUtils.isEmpty(userId)){
                                    Map<String, Object> body = new HashMap<>();
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("password", pwd);
                                    user.put("auth_type", authType);
                                    user.put("name", userName);
                                    body.put("user", user);
                                    ResponseEntity<String> userResponse = RestUtils.post(userUrl, body, String.class, rsParam.getAk(), rsParam.getSk());
                                    if (userResponse == null || !userResponse.getStatusCode().is2xxSuccessful()){
                                        logger.error("Create user {} failed : {}", userName, userResponse.getBody());
                                        Record record = new Record();
                                        record.setOperation("Create user " + userName).setResult("Failed").setRootCause(userResponse.getBody());
                                        result.setResult("Partial success");
                                        result.getMessage().add(record);
                                        continue;
                                    }
                                    Map<String, Object> userMap = (Map<String, Object>) JsonUtils.parseJsonStr2Map(userResponse.getBody()).get("user");
                                    userId = userMap.get("id").toString();
                                }else {
                                    logger.info("User {} is already existing", userName);
                                }

                                String addUserToGroupUrl = RestUtils.buildUrl(scProperties.getScheme(), scProperties.getHost(), scProperties.getPort().toString(), scProperties.getApi().get("addUserToGroup").
                                    replace("{group_id}", group.get(key).toString()).replace("{user_id}", userId));
                                ResponseEntity<String> addUserToGroupResponse = RestUtils.put(addUserToGroupUrl, null, String.class, rsParam.getAk(), rsParam.getSk());
                                if (addUserToGroupResponse == null || !addUserToGroupResponse.getStatusCode().is2xxSuccessful()){
                                    logger.error("Create user {} failed : {}", userName, addUserToGroupResponse.getBody());
                                    Record record = new Record();
                                    record.setOperation("Add user " + userName + " to " + key).setResult("Failed").setRootCause(addUserToGroupResponse.getBody());
                                    result.setResult("Partial success");
                                    result.getMessage().add(record);
                                }
                            }
                        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
                            logger.error("invoke users method error", e);
                            Record record = new Record();
                            record.setOperation("Get " + field + " users").setResult("Failed").setRootCause(e.getMessage());
                            result.setResult("Partial success");
                            result.getMessage().add(record);
                        }
                    }
                }
            }
        }
        return true;
    }

    private static List<Map<String, Object>> queryUsers(RsParam rsParam, String url) {
        int start = 0;
        int limit = 100;
        int total = 0;
        int dataTotal;
        Map<String, Object> query = new HashMap<>();
        query.put("start", start);
        query.put("limit", limit);
        List<Map<String, Object>> userList = new ArrayList<>();
        do {
            ResponseEntity<String> response = RestUtils.get(url, query, String.class, rsParam.getAk(), rsParam.getSk());
            if (response == null || !response.getStatusCode().is2xxSuccessful()){
                logger.error("Query users failed: {}", response.getBody());
            }
            dataTotal = (int) JsonUtils.parseJsonStr2Map(response.getBody()).get("total");
            userList.addAll((List<Map<String, Object>>)JsonUtils.parseJsonStr2Map(response.getBody()).get("users"));
            total += limit;
            start++;
        }while (total < dataTotal);
        return userList;
    }
}
