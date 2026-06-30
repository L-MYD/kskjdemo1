package com.kskj.mapper;
import com.kskj.pojo.WmsWcsTaskInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper{
    void  add(WmsWcsTaskInfo wmsWcsTaskInfo);
    //查询所有
    List<WmsWcsTaskInfo> findAll();
    //查询单个
    WmsWcsTaskInfo findOne(String tesid);
    WmsWcsTaskInfo findByProgress(String Progress);
    List<WmsWcsTaskInfo> findByTaskStatus(String taskStatus);
    List<WmsWcsTaskInfo> findByTaskStatusAndTaskType(String taskStatus,String taskType,String rcsOrTes);
    List<WmsWcsTaskInfo> findByTaskStatusAndTaskTypeA(String taskStatus,String taskType,String rcsOrTes);
    WmsWcsTaskInfo findOneByThirdPartyTaskIdAndRcsOrTes(String ThirdPartyTaskId,String RcsOrTes);
    //修改
    List<WmsWcsTaskInfo> findByTaskStatusAndTaskTypeAb(String taskType, String rcsOrTes);
    void  updete(WmsWcsTaskInfo wmsWcsTaskInfo);
    void  updetet(WmsWcsTaskInfo wmsWcsTaskInfo);
    void  updeteStaus(WmsWcsTaskInfo wmsWcsTaskInfo);

    void  updeteTwo(WmsWcsTaskInfo wmsWcsTaskInfo);
    //删除
    void  delete(Long id);
    //获取总条数
    WmsWcsTaskInfo findOneByWcstaskId(String WCSTaskId);
    // 需要添加的方法
//    List<WmsWcsTaskInfo> findParentTasksByIds(@Param("parentIds") List<String> parentIds);
  List<WmsWcsTaskInfo> findChildTasksByParentIds(@Param("parentIds") List<String> parentIds);


}
