/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.ghca.adapter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * JSON对象、数组、字符串转换工具类
 *
 * @version v1.0
 * @description:
 * @author: jiahang on 2020.5.11.011 上午 09:29
 * @since 2022-03-12
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private JsonUtils() {
    }

    /**
     * 将JSON对象转换为Map
     *
     * @param jsonNode jsonNode
     * @param <K> K
     * @param <V> V
     * @return Map
     */
    public static <K, V> Map<K, V> parseJsonObject2Map(JsonNode jsonNode) {
        return parseJsonStr2Map(parseObject2Str(jsonNode));
    }

    /**
     * 将JSON数组转换为List
     *
     * @param <T> T
     * @param jsonNode jsonNode
     * @return List
     */
    public static <T> List<T> parseJsonArray2List(JsonNode jsonNode) {
        return parseJsonStr2List(parseObject2Str(jsonNode), new TypeReference<List<T>>() { });
    }

    /**
     * 将JSON字符串转换为List
     *
     * @param <T> T
     * @param jsonStr jsonStr
     * @return List
     */
    public static <T> List<T> parseJsonStr2List(String jsonStr) {
        return parseJsonStr2List(jsonStr, new TypeReference<List<T>>() { });
    }

    /**
     * 将JSON字符串转换为泛型List
     *
     * @param <T> T
     * @param jsonStr jsonStr
     * @param typeReference typeReference
     * @return List
     */
    public static <T> List<T> parseJsonStr2List(String jsonStr, TypeReference<List<T>> typeReference) {
        List<T> list = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            list = objectMapper.readValue(jsonStr, typeReference);
        } catch (JsonMappingException e) {
            logger.error("convert JSON to List failure : {}", e);
        } catch (JsonProcessingException e) {
            logger.error("convert JSON to List failure : {}", e);
        }
        return list;
    }

    /**
     * 将JSON字符串转换为泛型Map
     *
     * @param <K> K
     * @param <V> V
     * @param jsonStr jsonStr
     * @return Map
     */
    public static <K, V> Map<K, V> parseJsonStr2Map(String jsonStr) {
        Map<K, V> map = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            map = objectMapper.readValue(jsonStr, Map.class);
        } catch (JsonMappingException e) {
            logger.error("convert JSON to Map failure : {}", e);
        } catch (JsonProcessingException e) {
            logger.error("convert JSON to Map failure : {}", e);
        }
        return map;
    }

    /**
     * 将对象转为JSON字符串
     *
     * @param object object
     * @return String
     */
    public static String parseObject2Str(Object object) {
        String json = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(object);
        } catch (JsonMappingException e) {
            logger.error("convert JSON to Str failure : {}", e);
        } catch (JsonProcessingException e) {
            logger.error("convert JSON to Str failure : {}", e);
        }
        return json;
    }

    /**
     * 将json字符串转为java对象
     *
     * @param json json
     * @param clazz clazz
     * @param <T> T
     * @return T
     */
    public static <T> T parseJsonStr2Bean(String json, Class<T> clazz) {
        T javaObject = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // 忽略未知字段
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            javaObject = objectMapper.readValue(json, clazz);
        } catch (JsonMappingException e) {
            logger.error("convert JSON to Java Bean failure : {}", e);
        } catch (JsonProcessingException e) {
            logger.error("convert JSON to Java Bean failure : {}", e);
        }
        return javaObject;
    }

    /**
     * 将json字符串转为JsonNode
     *
     * @param json json
     * @return JsonNode
     */
    public static JsonNode parseJsonStr2JsonNode(String json) {
        JsonNode jsonNode = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readValue(json, JsonNode.class);
        } catch (JsonMappingException e) {
            logger.error("parseJsonStr2JsonNode failure : {}", e);
        } catch (JsonProcessingException e) {
            logger.error("parseJsonStr2JsonNode failure : {}", e);
        }
        return jsonNode;
    }

    /**
     * object2JsonNode
     *
     * @param obj obj
     * @return JsonNode
     */
    public static JsonNode object2JsonNode(Object obj) {
        return parseJsonStr2JsonNode(parseObject2Str(obj));
    }

    /**
     * list2ArrayNode
     *
     * @param <T> T
     * @param list list
     * @return ArrayNode
     */
    public static <T> ArrayNode list2ArrayNode(List<T> list) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        list.forEach(obj -> arrayNode.add(object2JsonNode(obj)));
        return arrayNode;
    }

    /**
     * writeValueAsString
     *
     * @param obj obj
     * @return String
     */
    public static String writeValueAsString(Object obj) {
        String jsonString = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonString = objectMapper.writeValueAsString(obj);
        } catch (JsonMappingException e) {
            logger.error("convert JSON to JsonNode failure : {}", e);
        } catch (JsonProcessingException e) {
            logger.error("convert JSON to JsonNode failure : {}", e);
        }
        return jsonString;
    }
}
