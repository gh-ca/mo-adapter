package com.ghca.adapter.controller;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.ResourceSpaceService;
import com.ghca.adapter.utils.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
    public Result resourceSpace(@RequestBody @Validated RsParam rsParam, BindingResult bindingResult){
        Result result = new Result();
        ArrayList<Record> records = new ArrayList<>();
        result.setMessage(records);
        if (bindingResult.hasErrors()){
            Record record = new Record();
            String errorInfo = JsonUtils.parseObject2Str(bindingResult.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(",")));
            logger.error("Param validation failed: {}", errorInfo);
            record.setOperation("Param validation").setResult("Failed").setRootCause(errorInfo);
            result.getMessage().add(record);
            result.setResult("Failed");
            return result;
        }
        return resourceSpaceService.createRs(rsParam, result);
    }
}
