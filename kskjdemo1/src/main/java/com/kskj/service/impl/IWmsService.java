package com.kskj.service.impl;


import cn.hutool.core.util.IdUtil;
import com.kskj.pojo.ResendMessage;
import com.kskj.pojo.TES.ReleaseStation;
import com.kskj.pojo.TES.ReleaseStationResqon;
import com.kskj.pojo.WMS.ReleaseStationsHttp;
import com.kskj.pojo.WMS.ReleaseStationsHttpResqon;
import com.kskj.pojo.WMS.WmsTask;
import com.kskj.pojo.WMS.WmsTaskReponse;
import com.kskj.pojo.rcs.Sation.UnBind;
import com.kskj.until.R;

import java.util.List;

public interface IWmsService {
    WmsTaskReponse[] AddTask(WmsTask[] wmsTask);
    R CancelTask(String  wcsTaskId);
    R ReleaseStations(String PalnNo);
    R ReleaseRcsStation(UnBind unBind);
    R plcAddTask(WmsTask wmsTask);
    List<String> OutboundQuery(String locationtype);
    R WmsResendMessage(ResendMessage resendMessage);
}
