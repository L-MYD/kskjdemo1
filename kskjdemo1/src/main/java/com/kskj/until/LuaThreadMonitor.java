package com.kskj.until;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.stereotype.Component;

@Component
public class LuaThreadMonitor {
    private final Globals globals;
    private final LuaValue luaScript;

    public LuaThreadMonitor() {
        // 初始化 Lua 环境
        globals = JsePlatform.standardGlobals();

        // 加载并执行 Lua 监控脚本
        String luaCode =
                "local monitor = {}\n" +
                        "monitor.logs = {}\n" +
                        "\n" +
                        "function monitor:log(method, threadId, timestamp, status)\n" +
                        "    local entry = {\n" +
                        "        method = method,\n" +
                        "        thread = threadId,\n" +
                        "        time = timestamp,\n" +
                        "        status = status,\n" +
                        "        order = #self.logs + 1\n" +
                        "    }\n" +
                        "    table.insert(self.logs, entry)\n" +
                        "    \n" +
                        "    -- 实时输出\n" +
                        "    print(string.format('[%s][线程%d][顺序%d] %s - %s', \n" +
                        "        os.date('%H:%M:%S'), threadId, entry.order, method, status))\n" +
                        "end\n" +
                        "\n" +
                        "function monitor:analyze()\n" +
                        "    print('=== Lua线程监控报告 ===')\n" +
                        "    print('总记录数: ' .. #self.logs)\n" +
                        "    \n" +
                        "    -- 线程统计\n" +
                        "    local threadStats = {}\n" +
                        "    for _, entry in ipairs(self.logs) do\n" +
                        "        threadStats[entry.thread] = (threadStats[entry.thread] or 0) + 1\n" +
                        "    end\n" +
                        "    \n" +
                        "    for threadId, count in pairs(threadStats) do\n" +
                        "        print('线程 ' .. threadId .. ': ' .. count .. ' 次执行')\n" +
                        "    end\n" +
                        "end\n" +
                        "\n" +
                        "return monitor";

        luaScript = globals.load(luaCode).call();
    }

    public void logMethod(String methodName, long threadId, String status) {
        try {
            LuaValue logFunction = luaScript.get("log");
            // 正确的调用方式
            logFunction.invoke(LuaValue.varargsOf(new LuaValue[] {
                    luaScript,  // self 参数
                    LuaValue.valueOf(methodName),
                    LuaValue.valueOf(threadId),
                    LuaValue.valueOf(System.currentTimeMillis()),
                    LuaValue.valueOf(status)
            }));
        } catch (Exception e) {
            System.err.println("Lua 执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void generateReport() {
        try {
            LuaValue analyzeFunction = luaScript.get("analyze");
            analyzeFunction.call(luaScript);
        } catch (Exception e) {
            System.err.println("Lua 报告生成错误: " + e.getMessage());
        }
    }
}
