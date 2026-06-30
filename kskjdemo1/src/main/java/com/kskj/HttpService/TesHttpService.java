package com.kskj.HttpService;

import com.kskj.pojo.TES.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class TesHttpService {
    private static final String  url1= "http://192.168.30.162:80/tes/apiv2/newMovePodTask";//创建容器搬运任务url
    private static final String  url2= "http://192.168.30.162:80/tes/apiv2/cancelTask";//任务取消url
    private static final String  url3= "http://192.168.30.162:80/tes/apiv2/releaseStation";//站点清除接口

    @Autowired
    private RestTemplate restTemplate; // 注入Spring管理的RestTemplate
    //此方法为创建TES任务请求
    public TaskResqon AddTesTask(TesTask tesTask) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<TesTask> requestEntity = new HttpEntity<>(tesTask, headers);

            // 发送POST请求
            ResponseEntity<TaskResqon> response = restTemplate.exchange(
                    url1,
                    HttpMethod.POST,
                    requestEntity,
                    TaskResqon.class
            );
            TaskResqon body = response.getBody();
            return body;
        } catch (Exception e) {
            // 创建一个失败的TaskResqon对象返回
            System.out.println("下发TES任务失败："+e);
            TaskResqon failResponse = new TaskResqon();
            failResponse.setReturnCode(-1); // 可以设置为错误码，如-1表示请求失败
            failResponse.setReturnMsg("fail");
            failResponse.setReturnUserMsg("请求TES服务失败: " + e.getMessage()+"-------已将对应任务加入任务创建服务中");
            // data可以为null
            return failResponse;
        }
    }
//    public TaskResqon AddTesTask(TesTask tesTask) {
//        //申请库位，url为url2,post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
//        try {
//            // 设置请求头
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            // 直接使用wmsLocation对象创建请求实体
//            HttpEntity<TesTask> requestEntity = new HttpEntity<>(tesTask, headers);
//
//            // 发送POST请求
//            ResponseEntity<TaskResqon> response = restTemplate.exchange(
//                    url1,
//                    HttpMethod.POST,
//                    requestEntity,
//                    TaskResqon.class
//            );
//            TaskResqon body = response.getBody();
//            return body;
//        } catch (Exception e) {
//            throw new RuntimeException("创建TES任务失败", e);
//
//        }
//    }
    //此方法为取消任务请求
    public TesTaskCancelResqon CancelTask(TesTaskCancel tesTaskCancel) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<TesTaskCancel> requestEntity = new HttpEntity<>(tesTaskCancel, headers);

            // 发送POST请求
            ResponseEntity<TesTaskCancelResqon> response = restTemplate.exchange(
                    url2,
                    HttpMethod.POST,
                    requestEntity,
                    TesTaskCancelResqon.class
            );
            TesTaskCancelResqon body = response.getBody();
            return body;
        } catch (Exception e) {
            throw new RuntimeException("TES任务取消接口调用失败", e);

        }
    }
    //此请求为站点容器消除接口
    public ReleaseStationResqon ReleaseStationMet(ReleaseStation releaseStation) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<ReleaseStation> requestEntity = new HttpEntity<>(releaseStation, headers);
            // 发送POST请求
            ResponseEntity<ReleaseStationResqon> response = restTemplate.exchange(
                    url3,
                    HttpMethod.POST,
                    requestEntity,
                    ReleaseStationResqon.class
            );
            ReleaseStationResqon body = response.getBody();
            return body;
        } catch (Exception e) {
            throw new RuntimeException("TES站点容器注销接口调用失败", e);
        }
    }

}
