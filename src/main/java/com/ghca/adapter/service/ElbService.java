package com.ghca.adapter.service;

import com.ghca.adapter.model.req.ElbParam;
import com.ghca.adapter.model.resp.Result;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/3/5 10:49
 */
public interface ElbService {

    Result createAndBindQoS(ElbParam elbParam, Result result);

}
