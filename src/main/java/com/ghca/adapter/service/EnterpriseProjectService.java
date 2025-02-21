package com.ghca.adapter.service;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Result;

import org.springframework.http.ResponseEntity;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 16:49
 */
public interface EnterpriseProjectService {

    boolean createEnterpriseProjectInProject(RsParam rsParam, Result result);

    boolean isExist(RsParam rsParam, Result result);
}
