package com.kskj.service;

import cn.hutool.core.util.IdUtil;
import com.kskj.domian.BackupQuery;
import com.kskj.domian.DeviceState;
import com.kskj.domian.FaultCode;
import com.kskj.mapper.DbBackupMapper;
import com.kskj.mapper.DeviceStateMapper;
import com.kskj.pojo.*;
import com.kskj.service.impl.IDbBackupService;
import com.kskj.service.impl.IDeviceStateService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DeviceStateService implements IDeviceStateService {
    @Autowired
    private DeviceStateMapper deviceStateMapper;


    @Override
    public List<DeviceState> showDevice() {
        List<DeviceState> device = deviceStateMapper.findDevice();
        return device;
    }

    @Override
    public List<DeviceState> showStackerWorkState(int stackerNo) {
//        ArrayList<TvShow> tvShows = new ArrayList<>();
//        List<DeviceState> byProperty = deviceStateMapper.findByProperty(stackerNo);
//        if (byProperty != null && byProperty.size() > 0) {
//            TvShow tvShow = new TvShow();
//            int stackerState = ((DeviceState)byProperty.get(0)).getStackerState();
//            int noFinIshTaskNum;
//            List stackers;
//            //如果等于1的话就是1的话就是故障，2是待机，3是工作中，
//            if (stackerState == 1) {
//                tvShow.setStackerState("故障");
//                noFinIshTaskNum = ((DeviceState)byProperty.get(0)).getFaultNo();
//                //根据没有完成的任务编号去查询
//                stackers = deviceStateMapper.findByProperty( noFinIshTaskNum);
//                String faultDesc = "";
//                if (stackers != null && stackers.size() > 0) {
//                    faultDesc = ((FaultCode)stackers.get(0)).getFaultDesc();
//                }
//                Fault fault = new Fault();
//                fault.setFaultDesc(faultDesc);
//                tvShow.setFault(fault);
//            } else if (stackerState == 2) {
//                tvShow.setStackerState("待机");
//            } else if (stackerState == 3) {
//                tvShow.setStackerState("工作中");
//            }
//
//            tvShow.setStackerNo(stackerNo + "号堆垛机");
//            noFinIshTaskNum = this.outTrayListDAO.getNoFinishNum(stackerNo);
//            tvShow.setNoFinIshTaskNum(noFinIshTaskNum);
//            stackers = this.stackerDAO.findByProperty("stackerNo", stackerNo);
//            int currentTaskType = ((Stacker)stackers.get(0)).getCurrentTaskType();
//            if (currentTaskType == 1) {
//                tvShow.setCurrentTask("无 ");
//            } else if (currentTaskType == 2) {
//                tvShow.setCurrentTask("入盘 ");
//            } else if (currentTaskType == 3) {
//                tvShow.setCurrentTask("取盘");
//            } else if (currentTaskType == 4) {
//                tvShow.setCurrentTask("出库");
//                OutInfo outInfo = this.getOutInfoData(stackerNo);
//                tvShow.setOutInfo(outInfo);
//            } else if (currentTaskType == 5) {
//                tvShow.setCurrentTask("移库");
//                List<Map<String, Object>> moveList = this.stackerDAO.getMoveList(stackerNo);
//                String trayCode = "";
//                String srcKuwei = "";
//                String dstKuwei = "";
//                if (moveList != null && moveList.size() > 0) {
//                    trayCode = String.valueOf(((Map)moveList.get(0)).get("trayCode"));
//                    srcKuwei = String.valueOf(((Map)moveList.get(0)).get("code1"));
//                    dstKuwei = String.valueOf(((Map)moveList.get(0)).get("code2"));
//                }
//
//                MoveKu moveKu = new MoveKu();
//                moveKu.setTrayCode(trayCode);
//                moveKu.setSrcKuwei(srcKuwei);
//                moveKu.setDstKuwei(dstKuwei);
//                tvShow.setMoveKu(moveKu);
//            } else if (currentTaskType == 6) {
//                tvShow.setCurrentTask("盘点");
//                tvShow.setTrayCode("A0002");
//            }
//
//            pcShowLists.add(tvShow);
//        }
        return null;
    }


}
