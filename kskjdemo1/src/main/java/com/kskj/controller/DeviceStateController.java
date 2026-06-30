package com.kskj.controller;

import com.kskj.domian.DeviceState;
import com.kskj.service.DeviceStateService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/devicestate")
public class DeviceStateController {
    @Autowired
    DeviceStateService  deviceStateService;
//显示堆垛机的状态，并返回给前端
    @GetMapping("/showDevice")
    public R showDevice() {
        try {
            List<DeviceState>  list=deviceStateService.showDevice();
            return R.ok("success",list);
        } catch (Exception var2) {
//            log.error("showTv异常" + var2);
//            log.error((Object)null, var2);
            return R.fail("显示异常");
        }

    }
    @GetMapping("/showTv")
    public R showTv(@PathVariable("stackerNo") int stackerNo) {
        try {
            List<DeviceState> deviceStates = deviceStateService.showStackerWorkState(stackerNo);

            return R.ok("success",deviceStates);
        } catch (Exception var2) {
//            log.error("showTv异常" + var2);
//            log.error((Object)null, var2);
            return R.fail();
        }
    }

}
