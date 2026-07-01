package com.kskj.HttpService;

import com.kskj.pojo.ApiTask;
import com.kskj.pojo.WMS.WmsLocation;
import com.kskj.pojo.WMS.WmsLocationResqon;
import com.kskj.pojo.WcsResonse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WcsHttpService {
    // 通过构造函数注入
    @Autowired
    private RestTemplate restTemplate; // 注入Spring管理的RestTemplate
    private static final String url1 = "http://localhost:44338/api/task/addTask";//为wcs-api将新建的任务传递给我们页面端
    private static final String url2 = "http://localhost:44338/api/task/carTask";//任务开始执行时，将载具对应的任务发送我们页面端
    private static final String url3 = "http://localhost:44376/api/Wakeup/plcapi";//任务结束时，将下发灭菌区提升机的指令发给PLC接口
    //此方法为向wcs页面发送任务的数据接口。
    //此方法的用的地方为，wms新建任务完成之后，我们向tes或Rcs创建任务之后，将这些数据传入页面端
    public void applyLocation(ApiTask apiTask) {
        //申请库位，url为url2,post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<ApiTask> requestEntity = new HttpEntity<>(apiTask, headers);

            // 发送POST请求
            ResponseEntity<WcsResonse> response = restTemplate.exchange(
                    url1,
                    HttpMethod.POST,
                    requestEntity,
                    WcsResonse.class
            );


        } catch (Exception e) {
            throw new RuntimeException("库位申请接口调用失败", e);

        }
    }
    //现在上面的接口把任务给到我们页面端了，现在如果是tes给我们发任务状态的时候，需要去根据我们任务号，去查询任务状态，主要
    //需要执行这个任务的ps或agv信息，并且将这个Tes任务id发送给我页面端。
    //如果是rcs的话，在我新建完成任务后，因为这里需要指定AGV来搬运，如果任务创建成功之后，就应该把这个任务id,rootid发送给我页面端。
    //而且我在发送Rcs任务的时候还需要去查询我的页面端，某个区域的agv是否有空闲，不能让rcs系统去自动分配AGV.
// 这个方法主要是给页面端发送AGV/ps的对应的任务的方法
    public void sendAgvOrPsTask(ApiTask apiTask) {
        //申请库位，url为url2,post请求，参数为wmsLocation的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<ApiTask> requestEntity = new HttpEntity<>(apiTask, headers);

            // 发送POST请求
            ResponseEntity<WcsResonse> response = restTemplate.exchange(
                    url2,
                    HttpMethod.POST,
                    requestEntity,
                    WcsResonse.class
            );


        } catch (Exception e) {
            throw new RuntimeException("库位申请接口调用失败", e);

        }
    }

    // 这个方法主要是任务结束时，将下发灭菌区提升机的指令发给PLC接口
    public WcsResonse sendPLCTask(String podid) {
        //申请库位，url为url2,post请求，参数为podid的json格式，返回值类型为WmsLocationResqon
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 直接使用wmsLocation对象创建请求实体
            HttpEntity<String> requestEntity = new HttpEntity<>(podid, headers);

            // 发送POST请求
            ResponseEntity<WcsResonse> response = restTemplate.exchange(
                    url3,
                    HttpMethod.POST,
                    requestEntity,
                    WcsResonse.class
            );

            WcsResonse body = response.getBody();
            return body;

        } catch (Exception e) {
            throw new RuntimeException("PLC接口调用失败", e);

        }
    }
}
