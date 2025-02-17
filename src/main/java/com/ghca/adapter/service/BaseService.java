package com.ghca.adapter.service;

import com.ghca.adapter.model.properties.ScProperties;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/12 14:51
 */
public abstract class BaseService {

    @Autowired
    protected ScProperties scProperties;

}
