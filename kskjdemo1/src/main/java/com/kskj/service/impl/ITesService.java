package com.kskj.service.impl;

import com.kskj.pojo.TES.StationStatusMessage;
import com.kskj.pojo.TES.TaskStatusMessage;
import com.kskj.pojo.TES.TrackExternalInspection;
import com.kskj.pojo.TES.TrackExternallnspectionResqon;

public interface ITesService {

    TrackExternallnspectionResqon WarehouseLocation(TrackExternalInspection trackExternalInspection);
    public TrackExternallnspectionResqon ToWmsTaskInfo(TaskStatusMessage taskStatusMessage);
    public TrackExternallnspectionResqon getSiteMessage(StationStatusMessage stationStatusMessage);
}
