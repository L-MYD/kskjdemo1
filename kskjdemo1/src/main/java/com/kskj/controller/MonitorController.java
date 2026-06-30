package com.kskj.controller;

import com.kskj.until.LuaThreadMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor")
public class MonitorController {
    @Autowired
    private LuaThreadMonitor luaMonitor;

    @GetMapping("/report")
    public String generateReport() {
        luaMonitor.generateReport();
        return "监控报告已生成，请查看控制台";
    }

    @GetMapping("/reset")
    public String resetMonitor() {
        // 可以添加重置方法到 Lua 脚本中
        return "监控器已重置";
    }
}
