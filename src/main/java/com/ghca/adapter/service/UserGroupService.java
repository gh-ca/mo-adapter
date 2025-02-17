package com.ghca.adapter.service;

import com.ghca.adapter.model.req.RsParam;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
public interface UserGroupService {

    List<Map<String, Object>> createUserGroupAndAddRoles(RsParam rsParam, String projectId, String enterpriseProjectId);
}
