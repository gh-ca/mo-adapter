package com.ghca.adapter.controller;

import com.ghca.adapter.model.req.ElbParam;
import com.ghca.adapter.model.resp.Record;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.ElbService;
import com.ghca.adapter.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 14:11
 */
@Api
@RestController
@RequestMapping("/")
public class ElbController {

    private static Logger logger = LoggerFactory.getLogger(ElbController.class);

    @Resource
    private ElbService elbService;

    @ApiOperation(value = "ELB Api", httpMethod = "POST")
    @ApiResponse(code = 200, message = "Create success", response = Result.class)
    @PostMapping("resource-elb")
    public Result resourceSpace(@RequestBody @Validated ElbParam elbParam, BindingResult bindingResult){
        logger.info("Start bind QoS");
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
        return elbService.createAndBindQoS(elbParam, result);
    }
}
