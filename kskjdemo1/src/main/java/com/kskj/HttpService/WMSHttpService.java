package com.kskj.HttpService;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kskj.pojo.WMS.*;
import com.kskj.until.ExceptionLogger;
import com.kskj.until.SqlLogger;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

@Service
public class WMSHttpService {
    private static final String url1 = "http://192.168.30.129:8088/api/WMS/DeviceStatu";//设备任务状态上报url
    private static final String url2 = "http://192.168.30.129:8088/api/WMS/PalnoApply";//托盘入库申请url
    private static final String url3 = "http://192.168.30.129:8088/api/WMS/WCSTaskFinish";//任务完成接口url
    private static final String url4 = "http://192.168.30.129:8088/WMS/Bqs/LED";//LED 显示url
    private static final String url5 = "http://192.168.30.129:8088/api/WMS/RedoInMstore";//入库任务下发失败，回调wmsurl
    private static final String url6 = "http://192.168.30.129:8088/api/WMS/PalnoRelease";//站点信息清除url
    private static final String url8 = "http://192.168.30.129:8088/api/WMS/MJQPalnoApply";//灭菌区申请入库
    private static final String url9 = "http://192.168.30.129:8088/api/WMS/JXPalnoApply";//灭菌区申请入库
    private static final String url7 = "http://192.168.30.127:8030/api/Imotion/PalnoApply";//临时的url
    // 直接创建RestTemplate实例
    @Autowired
    private RestTemplate restTemplate; // 注入Spring管理的RestTemplate

    //此方法为向wms申请托盘入库方法
    public WmsLocationResqon applyLocation(WmsLocation wmsLocation) {
        //申请库位，url为  url2,   post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<WmsLocation> requestEntity = new HttpEntity<>(wmsLocation, headers);

            // 发送POST请求
            ResponseEntity<WmsLocationResqon> response = restTemplate.exchange(
                    url2,
                    HttpMethod.POST,
                    requestEntity,
                    WmsLocationResqon.class
            );

            WmsLocationResqon body = response.getBody();
            return body;


        } catch (Exception e) {
            throw new RuntimeException("库位申请接口调用失败", e);

        }
    }
    public WmsLocationResqon applyRcsLocation(WmsLocation wmsLocation) {
        //申请库位，url为  url2,   post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<WmsLocation> requestEntity = new HttpEntity<>(wmsLocation, headers);

            // 发送POST请求
            ResponseEntity<WmsLocationResqon> response = restTemplate.exchange(
                    url8,
                    HttpMethod.POST,
                    requestEntity,
                    WmsLocationResqon.class
            );

            WmsLocationResqon body = response.getBody();
            return body;


        } catch (Exception e) {
            throw new RuntimeException("解析区库位申请接口调用失败", e);

        }
    }
    public WmsLocationResqon WcapplyRcsLocation(WmsLocation wmsLocation) {
        //申请库位，url为  url2,   post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<WmsLocation> requestEntity = new HttpEntity<>(wmsLocation, headers);

            // 发送POST请求
            ResponseEntity<WmsLocationResqon> response = restTemplate.exchange(
                    url9,
                    HttpMethod.POST,
                    requestEntity,
                    WmsLocationResqon.class
            );

            WmsLocationResqon body = response.getBody();
            return body;


        } catch (Exception e) {
            throw new RuntimeException("解析完成库位申请接口调用失败", e);

        }
    }
    //此接口为入库申请成功之后，由于各种原因创建小车搬运任务失败，由此返回给wms。
    public WmsLocationResqon RedoInMstore(WmsLocation wmsLocation) {
        //申请库位，url为url2,post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<WmsLocation> requestEntity = new HttpEntity<>(wmsLocation, headers);

            // 发送POST请求
            ResponseEntity<WmsLocationResqon> response = restTemplate.exchange(
                    url5,
                    HttpMethod.POST,
                    requestEntity,
                    WmsLocationResqon.class
            );

            WmsLocationResqon body = response.getBody();
            return body;


        } catch (Exception e) {
            throw new RuntimeException("返回给wms接口调用失败", e);

        }
    }

    //此方法为向wms发送任务状态方法

    public WmsStatusResponse sendTaskStatus(WmsStatusItem wmsStatusItem) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<WmsStatusItem> requestEntity = new HttpEntity<>(wmsStatusItem, headers);

            // 发送POST请求
            ResponseEntity<WmsStatusResponse> response = restTemplate.exchange(
                    url3,
                    HttpMethod.POST,
                    requestEntity,
                    WmsStatusResponse.class
            );

            WmsStatusResponse body = response.getBody();
            return body;


        } catch (Exception e) {
            throw new RuntimeException("回调wms任务状态接口失败", e);

        }
//        try {
//            // 1. 记录发送开始日志
//            SqlLogger.logSqlStart("发送任务状态到WMS", "WMS状态更新", wmsStatusItem);
//
//            // 2. 创建请求体（包装成数组格式）
////            List<WmsStatusItem> requestList = Collections.singletonList(wmsStatusItem);
//
//            // 3. 创建ObjectMapper并配置日期格式
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
//
//            // 4. 转换为JSON字符串
//            String requestBody = objectMapper.writeValueAsString(wmsStatusItem);
//
//            // 5. 创建HTTP头
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//            // 6. 创建HTTP实体
//            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//            // 7. 创建RestTemplate并发送请求
//            RestTemplate restTemplate = new RestTemplate();
//
//            // 使用ParameterizedTypeReference来接收集合类型
//            ResponseEntity<List<WmsStatusResponse>> response = restTemplate.exchange(
//                    url3,
//                    HttpMethod.POST,
//                    entity,
//                    new ParameterizedTypeReference<List<WmsStatusResponse>>() {}
//            );
//
//            // 8. 处理响应
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                List<WmsStatusResponse> responseList = response.getBody();
//
//                // 处理每个响应
//                for (WmsStatusResponse wmsResponse : responseList) {
//                    if ("S".equals(wmsResponse.getMsgType())) {
//                        SqlLogger.logSqlSuccess("发送任务状态到WMS - 项目: " + wmsResponse.getWcsId(),
//                                System.currentTimeMillis());
//                    } else {
//                        SqlLogger.logSqlError("发送任务状态到WMS",
//                                "WMS返回非成功状态: " + wmsResponse.getMsgText());
//                    }
//                }
//
//                return responseList;
//
//            } else {
//                // 记录错误日志
//                String errorMsg = "WMS返回非200状态码: " + response.getStatusCode();
//                SqlLogger.logSqlError("发送任务状态到WMS", errorMsg);
//                ExceptionLogger.logApplicationError(errorMsg, "sendTaskStatus");
//
//                // 返回一个包含错误响应的集合
//                return Collections.singletonList(
//                        new WmsStatusResponse("E", "HTTP错误: " + response.getStatusCode(),
//                                wmsStatusItem.getWcsId(), null)
//                );
//            }
//
//        } catch (Exception e) {
//            // 10. 异常处理
//            String errorMsg = "向WMS发送任务状态异常: " + e.getMessage();
//            SqlLogger.logSqlException("发送任务状态到WMS", e, "WMS接口调用异常");
//            ExceptionLogger.logException(e, "sendTaskStatus方法执行异常");
//
//            // 返回一个包含异常响应的集合
//            return Collections.singletonList(
//                    new WmsStatusResponse("E", "系统异常: " + e.getMessage(),
//                            wmsStatusItem.getWcsId(), null)
//            );
//        }
    }
    public ReleaseStationsHttpResqon ReleaseStationsHttp(ReleaseStationsHttp releaseStationsHttp) {

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<ReleaseStationsHttp> requestEntity = new HttpEntity<>(releaseStationsHttp, headers);

            // 发送POST请求
            ResponseEntity<ReleaseStationsHttpResqon> response = restTemplate.exchange(
                    url6,
                    HttpMethod.POST,
                    requestEntity,
                    ReleaseStationsHttpResqon.class
            );

            ReleaseStationsHttpResqon body = response.getBody();
            return body;


        } catch (Exception e) {
            throw new RuntimeException("调用wms站点清除接口失败", e);

        }
    }

}