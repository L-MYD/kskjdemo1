package com.kskj.until;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.logging.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

public class HttpPostUtil {
//    private static final Logger logger = LoggerFactory.getLogger(HttpPostUtil.class);
    private static final RestTemplate restTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        restTemplate = new RestTemplate();
        // 设置字符编码
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    /**
     * POST请求 - JSON格式（默认请求头）
     */
    public static String doPostJson(String url, Object requestBody) {
        return doPostJson(url, null, requestBody);
    }

    /**
     * POST请求 - JSON格式（自定义请求头）
     */
    public static String doPostJson(String url, Map<String, String> customHeaders, Object requestBody) {
        try {
            HttpHeaders headers = createHeaders(customHeaders);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
            return handleResponse(response);
        } catch (Exception e) {
//            logger.error("POST请求异常: {}", e.getMessage(), e);e
            throw new RuntimeException("HTTP POST请求失败", e);
        }
    }

    /**
     * POST请求 - Form表单格式
     */
    public static String doPostForm(String url, Map<String, String> formData) {
        return doPostForm(url, null, formData);
    }

    /**
     * POST请求 - Form表单格式（自定义请求头）
     */
    public static String doPostForm(String url, Map<String, String> customHeaders, Map<String, String> formData) {
        try {
            HttpHeaders headers = createHeaders(customHeaders);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 构建表单数据
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(entry.getKey())
                        .append("=")
                        .append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(formBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            return handleResponse(response);
        } catch (Exception e) {
//            logger.error("POST表单请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("HTTP POST表单请求失败", e);
        }
    }

    /**
     * POST请求 - 纯文本格式
     */
    public static String doPostText(String url, String text) {
        return doPostText(url, null, text);
    }

    /**
     * POST请求 - 纯文本格式（自定义请求头）
     */
    public static String doPostText(String url, Map<String, String> customHeaders, String text) {
        try {
            HttpHeaders headers = createHeaders(customHeaders);
            headers.setContentType(MediaType.TEXT_PLAIN);

            HttpEntity<String> requestEntity = new HttpEntity<>(text, headers);

//            logger.info("POST文本请求 URL: {}", url);
//            logger.debug("请求头: {}", headers);
//            logger.debug("文本内容: {}", text);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            return handleResponse(response);
        } catch (Exception e) {
//            logger.error("POST文本请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("HTTP POST文本请求失败", e);
        }
    }

    /**
     * 创建请求头
     */
    private static HttpHeaders createHeaders(Map<String, String> customHeaders) {
        HttpHeaders headers = new HttpHeaders();
        if (customHeaders != null) {
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                headers.add(entry.getKey(), entry.getValue());
            }
        }
        // 添加默认请求头
        headers.add("User-Agent", "Java-HTTP-Client/1.0");
        return headers;
    }

    /**
     * 处理响应
     */
    private static String handleResponse(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
//            logger.info("请求成功，状态码: {}", response.getStatusCodeValue());
            return response.getBody();
        } else {
//            logger.error("请求失败，状态码: {}, 响应体: {}",
//            response.getStatusCodeValue(), response.getBody());
            throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCodeValue());
        }
    }

    /**
     * 带超时设置的POST请求（需要配置RestTemplate）
     */
    public static String doPostWithTimeout(String url, Object requestBody, int timeoutMillis) {
        // 这里需要配置带有超时设置的RestTemplate
        // 实际使用时可以根据需要实现
        return doPostJson(url, requestBody);
    }
}
