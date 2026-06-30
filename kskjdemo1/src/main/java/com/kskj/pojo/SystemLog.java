package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    private int id;
    private String usertype;//使用者类型: system/web/api/TES/RCS,我们这里用户就用wcs-api
    private String logtype;//日志类型,这里日志类型分为：如果是WmsController的话就为WMS，
    // 如果是TesController的话就是Tes,RcsController的话就为Rcs,数据库操作就为DataBase,
    // 调用第三方请求就是如果是TesHttpService的话就为TesHttp,WMSHttpService的话就为WMSHttp,
    //RcsHttpService的话就为RcsHttp,
    private String level;//日志级别
    private String message;//日志内容
    private String module;//模块名称
    private String operation;//操作描述
    private String details;//详细内容
    private String userid;//操作人员ID
    private String ipaddress;//IP地址
    private String createtime;//创建时间
    private String isarchived;//是否已归档 默认为0

}
