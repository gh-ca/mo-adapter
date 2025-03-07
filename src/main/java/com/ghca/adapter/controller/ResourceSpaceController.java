package com.ghca.adapter.controller;

import com.ghca.adapter.model.req.RsParam;
import com.ghca.adapter.model.resp.Result;
import com.ghca.adapter.service.ResourceSpaceService;
import com.ghca.adapter.utils.Constant;
import com.ghca.adapter.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 14:11
 */
@Api
@RestController
@RequestMapping("/")
public class ResourceSpaceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSpaceController.class);

    @Resource
    private ResourceSpaceService resourceSpaceService;

    @ApiOperation(value = "IAM Api", httpMethod = "POST")
    @ApiResponse(code = 200, message = "Create success", response = Result.class)
    @PostMapping("resource-space-iam")
    public Result resourceSpace(@RequestBody @Validated RsParam rsParam, BindingResult bindingResult){
        LOGGER.info("Start create resource space");
        Result result = new Result("success");
        if (bindingResult.hasErrors()){
            String errorInfo = JsonUtils.parseObject2Str(bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(",")));
            LOGGER.error("Param validation failed: {}", errorInfo);
            return result.addMessage("Param validation", Constant.FAILED, errorInfo).setResult(Constant.FAILED);
        }
        return resourceSpaceService.createRs(rsParam, result);
    }
}
