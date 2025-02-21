package com.ghca.adapter.service;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Result;

import java.util.List;
import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
public interface UserService {

    boolean createUserInGroup(RsParam rsParam, String groupRolesRel, Result result);
}
