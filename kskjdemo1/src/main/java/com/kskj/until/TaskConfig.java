package com.kskj.until;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TaskConfig {
    private final Map<String, Integer> maxSubTasks = new HashMap<>();
    private Integer defaultMaxSubTasks = 1;

    @PostConstruct
    public void init() {
//        System.out.println("\n=== TaskConfig 硬编码配置初始化 ===");

        maxSubTasks.put("入成品库任务", 3);
        maxSubTasks.put("入待灭菌区任务", 2);
        maxSubTasks.put("入解析区任务", 2);
        maxSubTasks.put("灭菌任务", 1);
        maxSubTasks.put("待灭菌出发任务", 2);

        // 可以在这里添加更多配置
        // maxSubTasks.put("出库任务", 2);
        // maxSubTasks.put("盘点任务", 1);
        // maxSubTasks.put("移库任务", 2);

//        System.out.println("配置内容: " + maxSubTasks);
//        System.out.println("默认值: " + defaultMaxSubTasks);
//        System.out.println("=== TaskConfig 初始化完成 ===\n");
    }

    // Getter 和 Setter
    public Map<String, Integer> getMaxSubTasks() {

        return Collections.unmodifiableMap(maxSubTasks);
    }

    public void setMaxSubTasks(Map<String, Integer> maxSubTasks) {

    }

    public Integer getDefaultMaxSubTasks() {
        return defaultMaxSubTasks;
    }

    public void setDefaultMaxSubTasks(Integer defaultMaxSubTasks) {
        this.defaultMaxSubTasks = defaultMaxSubTasks;
    }

    /**
     * 根据任务类型获取最大子任务数 - 返回int类型
     */
    public int getMaxSubTasksByType(String taskType) {
        // 遍历Map，检查传入的任务类型是否以某个键开头
        for (Map.Entry<String, Integer> entry : maxSubTasks.entrySet()) {
            if (taskType.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultMaxSubTasks;
    }

    /**
     * 获取所有配置的任务类型
     */
    public Set<String> getSupportedTaskTypes() {
        return Collections.unmodifiableSet(maxSubTasks.keySet());
    }
}

