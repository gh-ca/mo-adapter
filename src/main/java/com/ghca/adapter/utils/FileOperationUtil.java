/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.ghca.adapter.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FileOperationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileOperationUtil.class);

    private static String staticBasePath;

    /**
     * basePath
     */
    @Value("${template.path:}")
    private String basePath;

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        staticBasePath = basePath;
    }

    /**
     * getFilePath
     *
     * @param fileName fileName
     * @return String
     */
    public static String getFilePath(String fileName) {
        String path = staticBasePath;
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(path)) {
            path = System.getProperty("user.dir");
            builder.append(path).append(File.separator).append("config").append(File.separator).append("template");
        } else {
            builder.append(path);
        }
        builder.append(File.separator).append(fileName);
        return builder.toString();
    }

    /**
     * 获取 getTemplate
     *
     * @param filePath filePath
     * @return String
     */
    public static String getTemplate(String filePath) {
        String template = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(filePath));
            template = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            LOGGER.error("Load template file error.", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Closing stream error.", e);
                }
            }
        }
        return template;
    }
}