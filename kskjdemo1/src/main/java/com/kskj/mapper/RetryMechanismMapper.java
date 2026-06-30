package com.kskj.mapper;


import com.kskj.pojo.ResendMessage;
import com.kskj.pojo.WmsWcsTaskInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 此类为wms接收任务消息失败后，将消息添加进数据库的mapper
 *
 * */

@Mapper
public interface RetryMechanismMapper  {
    void  add(ResendMessage resendMessage);
    //查询所有的未发送成功的消息
    List<ResendMessage> findAll(String issuccess);
    void  updete(ResendMessage resendMessage);
    //查询单个
//    WmsWcsTaskInfo findOne(String is);
}
