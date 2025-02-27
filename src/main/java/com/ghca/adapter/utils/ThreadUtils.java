package com.ghca.adapter.utils;

import java.util.Map;

public class ThreadUtils {

    private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    public static ThreadLocal<Map<String, Object>> getThreadLocal() {return threadLocal;}
}
