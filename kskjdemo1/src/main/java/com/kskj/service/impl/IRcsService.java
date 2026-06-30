package com.kskj.service.impl;

import com.kskj.pojo.rcs.reportertask.RcsReporterTask;
import com.kskj.pojo.rcs.reportertask.RcsReporterTaskResonse;

public interface IRcsService {
    RcsReporterTaskResonse RcsTaskToWms(RcsReporterTask rcsReporterTask);
}
