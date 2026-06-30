package com.kskj.until;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfigDiagnostic implements CommandLineRunner {

    @Autowired
    private ConfigurableEnvironment env;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== 配置诊断开始 ===");

        // 1. 检查所有配置源
        System.out.println("所有配置源:");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println("  - " + ps.getName());
        }

        // 2. 检查 task.config 相关的所有配置
        System.out.println("\n所有 task.config 相关的配置:");
        for (PropertySource<?> ps : env.getPropertySources()) {
            if (ps instanceof MapPropertySource) {
                Map<String, Object> source = ((MapPropertySource) ps).getSource();
                for (Map.Entry<String, Object> entry : source.entrySet()) {
                    String key = entry.getKey();
                    if (key.contains("task.config") || key.contains("TASK_CONFIG")) {
                        System.out.println("  " + key + " = " + entry.getValue() +
                                " (类型: " + entry.getValue().getClass().getSimpleName() +
                                ", 来源: " + ps.getName() + ")");
                    }
                }
            }
        }

        // 3. 特别检查冲突的配置
        System.out.println("\n关键配置检查:");
        String[] keys = {
                "task.config.max-sub-tasks",
                "TASK_CONFIG_MAX_SUB_TASKS",
                "task.limits.max-sub-tasks"
        };

        for (String key : keys) {
            String value = env.getProperty(key);
            System.out.println("  " + key + " = " + value);
        }

        System.out.println("=== 配置诊断结束 ===\n");
    }
}
