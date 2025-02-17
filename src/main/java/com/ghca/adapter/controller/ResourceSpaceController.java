package com.ghca.adapter.controller;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.service.ProjectService;
import com.ghca.adapter.service.ResourceSpaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 14:11
 */
@RestController
@RequestMapping("/")
public class ResourceSpaceController {

    private static Logger logger = LoggerFactory.getLogger(ResourceSpaceController.class);

    @Resource
    private ResourceSpaceService resourceSpaceService;

    @PostMapping("resource-space-iam")
    public Object resourceSpace(@RequestBody RsParam rsParam){
        resourceSpaceService.createRs(rsParam);
        return "";
    }
}
