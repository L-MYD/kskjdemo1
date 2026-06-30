package com.kskj.HttpService;

import com.kskj.pojo.rcs.RcsTaskCancel;
import com.kskj.pojo.rcs.RcsTaskCancelResonse;
import com.kskj.pojo.rcs.Sation.Bind;
import com.kskj.pojo.rcs.Sation.BindResonse;
import com.kskj.pojo.rcs.Sation.UnBind;
import com.kskj.pojo.rcs.Sation.UnBindResonse;
import com.kskj.pojo.rcs.Task.RcsCreaTask;
import com.kskj.pojo.rcs.Task.TaskResonse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class RcsHttpService {
    private static final String  url= "http://192.168.30.41:80/rcs/rtas/api/robot/controller/task";
    private static final String  url1= url+"/cancel";//任务取消url
    private static final String  url2= url+"/submit";//创建容器搬运任务url
    private static final String  url3= "http://192.168.30.41:80/rcs/rtas/api/robot/controller/carrier/bind";//站点绑定容器
    private static final String  url4= "http://192.168.30.41:80/rcs/rtas/api/robot/controller/carrier/unbind";//站点解绑容器
    @Autowired
    private RestTemplate restTemplate; // 注入Spring管理的RestTemplate
     /*
     //此方法为RCS任务取消请求
     */
    public RcsTaskCancelResonse CancelTask(RcsTaskCancel rcsTaskCancel) {
        try {
            HttpHeaders headers = generateRcsHeaders();
            HttpEntity<RcsTaskCancel> requestEntity = new HttpEntity<>(rcsTaskCancel, headers);

            ResponseEntity<RcsTaskCancelResonse> response = restTemplate.exchange(
                    url1,
                    HttpMethod.POST,
                    requestEntity,
                    RcsTaskCancelResonse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("RCS任务取消接口调用失败", e);
        }
    }
     /*
     此方法为创建rcs小车搬运任务
     */
    public TaskResonse NewRcsTask(RcsCreaTask rcsCreaTask){
        try {
            HttpHeaders headers = generateRcsHeaders();
            HttpEntity<RcsCreaTask> requestEntity = new HttpEntity<>(rcsCreaTask, headers);
            ResponseEntity<TaskResonse> response = restTemplate.exchange(
                    url2,
                    HttpMethod.POST,
                    requestEntity,
                    TaskResonse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("RCS任务取消接口调用失败", e);
        }
    }
    /*
此方法为站点绑定http请求
*/
    public BindResonse SationBind(Bind Bind){
        try {
            HttpHeaders headers = generateRcsHeaders();
            HttpEntity<Bind> requestEntity = new HttpEntity<>(Bind, headers);
            ResponseEntity<BindResonse> response = restTemplate.exchange(
                    url3,
                    HttpMethod.POST,
                    requestEntity,
                    BindResonse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("RCS站点上架失败", e);
        }
    }
    public UnBindResonse SationUnBind(UnBind unBind){
        try {
            HttpHeaders headers = generateRcsHeaders();
            HttpEntity<UnBind> requestEntity = new HttpEntity<>(unBind, headers);
            ResponseEntity<UnBindResonse> response = restTemplate.exchange(
                    url4,
                    HttpMethod.POST,
                    requestEntity,
                    UnBindResonse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("RCS站点解绑失败", e);
        }
    }
    /**
     * 生成RCS接口请求头
     */
    private HttpHeaders generateRcsHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-lr-request-id", UUID.randomUUID().toString());
        // headers.set("X-lr-version", "v1.0");
        // headers.set("X-lr-trace-id", UUID.randomUUID().toString());
        // headers.set("X-lr-source", "WCS");
        return headers;
    }
}
