package com.kskj.until;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

public class SqlLogger {
    private static final String LOG_DIRECTORY = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "YZBYKB-API_Logs";
    private static final String LOG_FILE_PATH = LOG_DIRECTORY + File.separator + "SQL_Operations.log";

    static {
        // 确保日志目录存在
        File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists()) {
            if (logDir.mkdirs()) {
                System.out.println("创建了SQL日志目录: " + LOG_DIRECTORY);
            } else {
                System.err.println("创建SQL日志目录失败: " + LOG_DIRECTORY);
            }
        }
    }

    /**
     * 记录SQL操作开始
     */
    public static void logSqlStart(String operation, String sql, Object parameters) {
        String logMessage = "[开始] " + operation + " - SQL: " + sql;

        if (parameters != null) {
            logMessage += "\n参数: " + serializeParameters(parameters);
        }

        writeLog("INFO", logMessage);
    }

    public static void logSqlStart(String operation, String sql) {
        logSqlStart(operation, sql, null);
    }

    /**
     * 记录SQL操作成功
     */
    public static void logSqlSuccess(String operation, long executionTime, Integer affectedRows) {
        String logMessage = "[成功] " + operation + " - 执行时间: " + executionTime + "ms";

        if (affectedRows != null) {
            logMessage += " - 影响行数: " + affectedRows;
        }

        writeLog("SUCCESS", logMessage);
    }

    public static void logSqlSuccess(String operation, long executionTime) {
        logSqlSuccess(operation, executionTime, null);
    }

    /**
     * 记录SQL查询结果
     */
    public static void logSqlResult(String operation, int resultCount) {
        String logMessage = "[结果] " + operation + " - 返回记录数: " + resultCount;
        writeLog("INFO", logMessage);
    }

    /**
     * 记录SQL操作失败
     */
    public static void logSqlError(String operation, String errorMessage, String sql) {
        String logMessage = "[失败] " + operation + " - 错误: " + errorMessage;

        if (sql != null && !sql.isEmpty()) {
            logMessage += "\nSQL: " + sql;
        }

        writeLog("ERROR", logMessage);
    }

    public static void logSqlError(String operation, String errorMessage) {
        logSqlError(operation, errorMessage, null);
    }

    /**
     * 记录SQL异常
     */
    public static void logSqlException(String operation, Exception ex, String sql) {
        String logMessage = "[异常] " + operation + " - 异常类型: " + ex.getClass().getSimpleName() + " - 消息: " + ex.getMessage();

        if (sql != null && !sql.isEmpty()) {
            logMessage += "\nSQL: " + sql;
        }

        logMessage += "\n堆栈跟踪: " + getStackTraceAsString(ex);

        writeLog("EXCEPTION", logMessage);
    }

    public static void logSqlException(String operation, Exception ex) {
        logSqlException(operation, ex, null);
    }

    private static void writeLog(String level, String message) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = dateFormat.format(new Date());
            String logEntry = timestamp + " [" + level + "] " + message;

            try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
                writer.write(logEntry + System.lineSeparator() + System.lineSeparator());
            }

            // 同时在控制台输出
            System.out.println(logEntry);
        } catch (Exception ex) {
            System.err.println("SQL日志写入失败: " + ex.getMessage());
            System.err.println("原日志内容: [" + level + "] " + message);
        }
    }

    private static String serializeParameters(Object parameters) {
        if (parameters == null) return "null";

        try {
            Field[] fields = parameters.getClass().getDeclaredFields();
            StringJoiner paramList = new StringJoiner(", ");

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(parameters);
                paramList.add(field.getName() + ": " + (value != null ? value.toString() : "null"));
            }

            return paramList.toString();
        } catch (Exception e) {
            return parameters.toString();
        }
    }

    private static String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取SQL日志文件路径
     */
    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }

    /**
     * 清空日志文件
     */
    public static void clearLogFile() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (logFile.exists()) {
                try (FileWriter writer = new FileWriter(LOG_FILE_PATH, false)) {
                    writer.write("");
                }
                writeLog("INFO", "日志文件已清空");
            }
        } catch (Exception ex) {
            System.err.println("清空日志文件失败: " + ex.getMessage());
        }
    }
}
