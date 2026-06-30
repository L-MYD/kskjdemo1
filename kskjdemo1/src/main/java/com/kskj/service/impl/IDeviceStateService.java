package com.kskj.service.impl;

import com.kskj.domian.BackupQuery;
import com.kskj.domian.DeviceState;
import com.kskj.pojo.DbBackup;
import com.kskj.until.R;

import javax.print.DocFlavor;
import java.util.List;

public interface IDeviceStateService {

    public List<DeviceState> showDevice();
    public List<DeviceState> showStackerWorkState(int stackerNo);

}
