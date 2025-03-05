package com.ghca.adapter.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2024/6/24 14:47
 */
@Component
public class LogBackThreadNameFilter implements Filter {

    public LogBackThreadNameFilter() {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.insertIntoMDC(request);
        try {
            chain.doFilter(request, response);
        } finally {
            this.clearMDC();
        }

    }

    void insertIntoMDC(ServletRequest request) {
        // 时间戳
        long timestamp = System.currentTimeMillis();

        // 随机数
        SecureRandom random = new SecureRandom();
        int randomNumber = random.nextInt(999999); // 生成六位数的随机数

        // 格式化为六位数，不足前面补0
        String formattedRandomNumber = String.format("%06d", randomNumber);

        // 拼接时间戳和随机数
        String id = formattedRandomNumber + timestamp;
        MDC.put("threadName", "trace-id:" + id);
    }

    void clearMDC() {
        MDC.remove("threadName");
    }

    public void init(FilterConfig arg0) throws ServletException {
    }
}
