package com.kskj.until;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;

public class ExceptionLogger {
    private static final String LOG_DIRECTORY = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "YZBYKB-API_Logs";
    private static final String LOG_FILE_PATH = LOG_DIRECTORY + File.separator + "Exception_Logs.log";

    static {
        // 确保日志目录存在
        try {
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                if (logDir.mkdirs()) {
                    System.out.println("创建了异常日志目录");
                }
            }

            // 写入初始化消息
            try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                writer.write(dateFormat.format(new Date()) + " - 异常日志系统初始化成功" + System.lineSeparator());
            }
            System.out.println("异常日志系统初始化完成");
        } catch (Exception ex) {
            System.err.println("异常日志初始化失败: " + ex.getMessage());
        }
    }

    /**
     * 记录一般异常
     */
    public static void logException(Exception ex, String additionalInfo) {
        try {
            String logMessage = "[异常] 类型: " + ex.getClass().getSimpleName() + "\n" +
                    "消息: " + ex.getMessage() + "\n" +
                    "堆栈跟踪: " + getStackTraceAsString(ex);

            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                logMessage += "\n附加信息: " + additionalInfo;
            }

            writeLog("ERROR", logMessage);
        } catch (Exception logEx) {
            System.err.println("记录异常失败: " + logEx.getMessage());
        }
    }

    public static void logException(Exception ex) {
        logException(ex, null);
    }

    /**
     * 记录SQL异常
     */
    public static void logSqlException(Exception ex, String sql, Object parameters) {
        try {
            String logMessage = "[SQL异常] 类型: " + ex.getClass().getSimpleName() + "\n" +
                    "消息: " + ex.getMessage() + "\n" +
                    "堆栈跟踪: " + getStackTraceAsString(ex);

            if (sql != null && !sql.isEmpty()) {
                logMessage += "\nSQL语句: " + sql;
            }

            if (parameters != null) {
                logMessage += "\n参数: " + serializeParameters(parameters);
            }

            writeLog("SQL_ERROR", logMessage);
        } catch (Exception logEx) {
            System.err.println("记录SQL异常失败: " + logEx.getMessage());
        }
    }

    public static void logSqlException(Exception ex, String sql) {
        logSqlException(ex, sql, null);
    }

    public static void logSqlException(Exception ex) {
        logSqlException(ex, null, null);
    }

    /**
     * 记录应用程序错误
     */
    public static void logApplicationError(String errorMessage, String module) {
        try {
            String logMessage = "[应用程序错误] 消息: " + errorMessage;

            if (module != null && !module.isEmpty()) {
                logMessage += "\n模块: " + module;
            }

            writeLog("APP_ERROR", logMessage);
        } catch (Exception ex) {
            System.err.println("记录应用程序错误失败: " + ex.getMessage());
        }
    }

    public static void logApplicationError(String errorMessage) {
        logApplicationError(errorMessage, null);
    }

    /**
     * 记录警告信息
     */
    public static void logWarning(String warningMessage, String source) {
        try {
            String logMessage = "[警告] 消息: " + warningMessage;

            if (source != null && !source.isEmpty()) {
                logMessage += "\n来源: " + source;
            }

            writeLog("WARNING", logMessage);
        } catch (Exception ex) {
            System.err.println("记录警告失败: " + ex.getMessage());
        }
    }

    public static void logWarning(String warningMessage) {
        logWarning(warningMessage, null);
    }

    /**
     * 记录重要信息
     */
    public static void logInfo(String infoMessage, String category) {
        try {
            String logMessage = "[信息] 消息: " + infoMessage;

            if (category != null && !category.isEmpty()) {
                logMessage += "\n分类: " + category;
            }

            writeLog("INFO", logMessage);
        } catch (Exception ex) {
            System.err.println("记录信息失败: " + ex.getMessage());
        }
    }

    public static void logInfo(String infoMessage) {
        logInfo(infoMessage, null);
    }

    /**
     * 获取异常日志文件路径
     */
    public static String getExceptionLogFilePath() {
        return LOG_FILE_PATH;
    }

    /**
     * 清空异常日志文件
     */
    public static void clearExceptionLogs() {
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (logFile.exists()) {
                try (FileWriter writer = new FileWriter(LOG_FILE_PATH, false)) {
                    writer.write("");
                }
                writeLog("SYSTEM", "异常日志文件已清空");
            }
        } catch (Exception ex) {
            System.err.println("清空异常日志文件失败: " + ex.getMessage());
        }
    }

    private static void writeLog(String level, String message) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = dateFormat.format(new Date());
            String logEntry = timestamp + " [" + level + "] " + message;

            try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
                writer.write(logEntry + System.lineSeparator() + System.lineSeparator());
            }

            System.out.println("异常日志: " + logEntry);
        } catch (Exception ex) {
            System.err.println("异常日志写入失败: " + ex.getMessage());
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
}
