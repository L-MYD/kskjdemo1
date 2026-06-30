//
//package com.kskj.until;
//
//import com.kskj.mapper.SystemLogMapper;
//import com.kskj.pojo.SystemLog;
//import com.kskj.pojo.TES.ReleaseStationResqon;
//import com.kskj.pojo.TES.TaskResqon;
//import com.kskj.pojo.TES.TesTaskCancelResqon;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//import java.lang.reflect.Parameter;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.Map;
//
//@Aspect
//@Component
//@Slf4j
//public class SystemLogAspect {
//    @Autowired
//    private SystemLogMapper systemLogMapper;
//
//    private ThreadLocal<SystemLog> logThreadLocal = new ThreadLocal<>();
//    private ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();
//    private ThreadLocal<String> requestArgsThreadLocal = new ThreadLocal<>();
//
//    // Controller切点定义
//    @Pointcut("execution(* com.kskj.controller.WmsController.*(..))")
//    public void wmsControllerPointcut() {}
//
//    @Pointcut("execution(* com.kskj.controller.TesController.*(..))")
//    public void tesControllerPointcut() {}
//
//    @Pointcut("execution(* com.kskj.controller.RcsController.*(..))")
//    public void rcsControllerPointcut() {}
//
//    // Service切点定义
//    @Pointcut("execution(* com.kskj.service..*.*(..))")
//    public void servicePointcut() {}
//
//    // 第三方HTTP调用切点
//    @Pointcut("execution(* com.kskj.HttpService.TesHttpService.*(..))")
//    public void tesHttpServicePointcut() {}
//
//    @Pointcut("execution(* com.kskj.HttpService.WMSHttpService.*(..))")
//    public void wmsHttpServicePointcut() {}
//
//    @Pointcut("execution(* com.kskj.HttpService.RcsHttpService.*(..))")
//    public void rcsHttpServicePointcut() {}
//
//    // Mapper切点定义 - 排除SystemLogMapper.add方法
//    @Pointcut("execution(* com.kskj.mapper..*.*(..)) && " +
//            "!execution(* com.kskj.mapper.SystemLogMapper.add(..)) && " +
//            "!execution(* com.kskj.mapper.TaskMapper.findByTaskStatusAndTaskType(..))")
//    public void mapperPointcut() {}
//
//    /**
//     * Controller层前置通知 - 增强版，记录方法参数
//     */
//    @Before("wmsControllerPointcut() || tesControllerPointcut() || rcsControllerPointcut()")
//    public void doBefore(JoinPoint joinPoint) {
//        try {
//            Long startTime = System.currentTimeMillis();
//            startTimeThreadLocal.set(startTime);
//
//            SystemLog systemLog = new SystemLog();
//            systemLog.setUsertype("wcs-api"); // 固定使用者类型
//            systemLog.setLevel("INFO");
//            systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//            systemLog.setIsarchived("0"); // 默认未归档
//            systemLog.setUserid(""); // 设置为空字符串
//
//            // 设置logtype基于Controller类型
//            String logtype = determineLogType(joinPoint);
//            systemLog.setLogtype(logtype);
//
//            // 获取请求信息
//            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            if (attributes != null) {
//                HttpServletRequest request = attributes.getRequest();
//                systemLog.setIpaddress(getClientIpAddress(request));
//
//                // 构建message和details
//                String message = buildRequestMessage(request, joinPoint);
//                systemLog.setMessage(message);
//
//                // 构建详情，包含方法参数
//                String details = buildRequestDetails(request, joinPoint);
//
//                // 添加方法参数信息
//                String methodParams = extractMethodParameters(joinPoint);
//                if (!methodParams.isEmpty()) {
//                    details += " | 方法参数: " + methodParams;
//                }
//
//                systemLog.setDetails(details);
//            }
//
//            // 设置模块和操作
//            extractModuleAndOperation(systemLog, joinPoint);
//
//            logThreadLocal.set(systemLog);
//
//        } catch (Exception e) {
//            log.error("记录请求日志前置处理异常: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * 第三方HTTP调用环绕通知 - 增强版本，记录请求和响应参数
//     */
//    @Around("tesHttpServicePointcut() || wmsHttpServicePointcut() || rcsHttpServicePointcut()")
//    public Object aroundHttpService(ProceedingJoinPoint point) throws Throwable {
//        Long startTime = System.currentTimeMillis();
//        String logtype = determineHttpLogType(point);
//        String methodName = point.getSignature().getName();
//
//        // 记录请求参数
//        String requestParams = buildHttpRequestParams(point);
//        String url = extractHttpUrl(point);
//
//        // 保存请求参数到ThreadLocal，用于异常时记录
//        requestArgsThreadLocal.set(requestParams);
//
//        try {
//            // 执行方法
//            Object result = point.proceed();
//
//            Long endTime = System.currentTimeMillis();
//            Long costTime = endTime - startTime;
//
//            // 检查返回结果：根据返回值判断是否成功
//            boolean isSuccess = checkResultIsSuccess(result);
//
//            // 记录响应参数
//            String responseParams = buildResponseParams(result);
//
//            if (isSuccess) {
//                // 创建成功日志
//                createAndSaveHttpLog(logtype, "INFO", "调用成功", url,
//                        methodName, costTime, null, requestParams, responseParams);
//                log.debug("HTTP调用成功 - 类型: {}, URL: {}, 方法: {}, 耗时: {}ms",
//                        logtype, url, methodName, costTime);
//            } else {
//                // 创建失败日志
//                String errorMsg = extractErrorMessageFromResult(result);
//                createAndSaveHttpLog(logtype, "ERROR", "调用失败", url,
//                        methodName, costTime, errorMsg, requestParams, responseParams);
//
//                log.error("HTTP调用返回失败 - 类型: {}, URL: {}, 方法: {}, 耗时: {}ms, 错误: {}",
//                        logtype, url, methodName, costTime, errorMsg);
//            }
//
//            return result;
//        } catch (Exception e) {
//            Long endTime = System.currentTimeMillis();
//            Long costTime = endTime - startTime;
//
//            // 获取之前保存的请求参数
//            String requestParamsOnError = requestArgsThreadLocal.get();
//
//            // 创建失败日志（含异常）
//            createAndSaveHttpLog(logtype, "ERROR", "调用异常", url,
//                    methodName, costTime, e.getMessage(), requestParamsOnError, "N/A - 调用异常");
//
//            // 记录详细异常
//            log.error("HTTP服务调用异常 - 类型: {}, URL: {}, 方法: {}, 耗时: {}ms, 请求参数: {}",
//                    logtype, url, methodName, costTime, requestParamsOnError, e);
//
//            throw e;
//        } finally {
//            requestArgsThreadLocal.remove();
//        }
//    }
//
//    /**
//     * 构建HTTP请求参数字符串（简化版）
//     */
//    private String buildHttpRequestParams(ProceedingJoinPoint point) {
//        try {
//            Object[] args = point.getArgs();
//            MethodSignature signature = (MethodSignature) point.getSignature();
//            Parameter[] parameters = signature.getMethod().getParameters();
//
//            StringBuilder paramsBuilder = new StringBuilder();
//
//            for (int i = 0; i < args.length; i++) {
//                if (i > 0) paramsBuilder.append("; ");
//
//                String paramName = parameters.length > i ? parameters[i].getName() : "arg" + i;
//                Object paramValue = args[i];
//
//                // 简化显示：只显示参数名和类型+值摘要
//                if (paramValue == null) {
//                    paramsBuilder.append(paramName).append(": null");
//                } else if (paramValue instanceof String) {
//                    String strValue = (String) paramValue;
//                    if (strValue.length() > 100) {
//                        paramsBuilder.append(paramName).append(": ").append(strValue, 0, 100).append("...");
//                    } else {
//                        paramsBuilder.append(paramName).append(": \"").append(strValue).append("\"");
//                    }
//                } else if (paramValue.getClass().isArray()) {
//                    paramsBuilder.append(paramName).append(": ").append(getArraySummary(paramValue));
//                } else {
//                    // 尝试toString，如果太长则截断
//                    String str = paramValue.toString();
//                    if (str.length() > 200) {
//                        paramsBuilder.append(paramName).append(": ").append(str, 0, 200).append("...");
//                    } else {
//                        paramsBuilder.append(paramName).append(": ").append(str);
//                    }
//                }
//            }
//
//            return paramsBuilder.toString();
//        } catch (Exception e) {
//            log.warn("构建HTTP请求参数失败: {}", e.getMessage());
//            return "参数解析失败: " + e.getMessage();
//        }
//    }
//
//    /**
//     * 获取数组的摘要信息
//     */
//    private String getArraySummary(Object array) {
//        if (array == null) return "null";
//
//        if (array instanceof Object[]) {
//            Object[] objArray = (Object[]) array;
//            return String.format("数组[长度=%d, 内容=%s...]",
//                    objArray.length,
//                    Arrays.toString(Arrays.copyOf(objArray, Math.min(objArray.length, 3))));
//        }
//
//        return array.getClass().getSimpleName() + "[...]";
//    }
//
//    /**
//     * 构建响应参数字符串
//     */
//    private String buildResponseParams(Object result) {
//        if (result == null) {
//            return "null";
//        }
//
//        try {
//            // 对于已知的响应类型，提取关键信息
//            if (result instanceof TaskResqon) {
//                TaskResqon resqon = (TaskResqon) result;
//                return String.format("TaskResqon[code=%d, msg=%s, userMsg=%s]",
//                        resqon.getReturnCode(),
//                        resqon.getReturnMsg(),
//                        resqon.getReturnUserMsg());
//            }
//
//            if (result instanceof TesTaskCancelResqon) {
//                TesTaskCancelResqon resqon = (TesTaskCancelResqon) result;
//                return String.format("TesTaskCancelResqon[code=%s, msg=%s]",
//                        resqon.getReturnCode(),
//                        resqon.getReturnMsg());
//            }
//
//            if (result instanceof ReleaseStationResqon) {
//                ReleaseStationResqon resqon = (ReleaseStationResqon) result;
//                return String.format("ReleaseStationResqon[code=%d, msg=%s]",
//                        resqon.getReturnCode(),
//                        resqon.getReturnMsg());
//            }
//
//            // 其他类型：使用toString并限制长度
//            String strResult = result.toString();
//            if (strResult.length() > 300) {
//                return strResult.substring(0, 300) + "...[已截断]";
//            }
//            return strResult;
//        } catch (Exception e) {
//            return "结果解析失败: " + e.getMessage();
//        }
//    }
//
//    /**
//     * 创建并保存HTTP调用日志（增强版）
//     */
//    private void createAndSaveHttpLog(String logtype, String level, String message,
//                                      String url, String method, Long costTime,
//                                      String errorMsg, String requestParams, String responseParams) {
//        SystemLog systemLog = new SystemLog();
//        systemLog.setUsertype("wcs-api");
//        systemLog.setLogtype(logtype);
//        systemLog.setLevel(level);
//        systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        systemLog.setIsarchived("0");
//        systemLog.setUserid("");
//        systemLog.setModule("HTTP_CLIENT");
//        systemLog.setOperation(method);
//
//        // 构建消息
//        String fullMessage = String.format("%s: %s - %s", message, method, url != null ? url : "Unknown URL");
//        systemLog.setMessage(fullMessage);
//
//        // 构建详细的details
//        StringBuilder details = new StringBuilder();
//        details.append("URL: ").append(url != null ? url : "N/A");
//        details.append(" | 方法: ").append(method);
//
//        if (costTime != null) {
//            details.append(String.format(" | 耗时: %dms", costTime));
//        }
//
//        if (requestParams != null && !requestParams.isEmpty()) {
//            details.append(" | 请求参数: ").append(requestParams);
//        }
//
//        if (responseParams != null && !responseParams.isEmpty()) {
//            details.append(" | 响应结果: ").append(responseParams);
//        }
//
//        if (errorMsg != null) {
//            details.append(" | 错误信息: ").append(errorMsg);
//        }
//
//        systemLog.setDetails(details.toString());
//
//        // 异步保存日志
//        asyncSaveLog(systemLog);
//    }
//
//    /**
//     * 提取方法参数信息 - 新增的方法
//     */
//    private String extractMethodParameters(JoinPoint joinPoint) {
//        try {
//            Object[] args = joinPoint.getArgs();
//            if (args == null || args.length == 0) {
//                return "无参数";
//            }
//
//            StringBuilder paramsBuilder = new StringBuilder();
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            String[] paramNames = signature.getParameterNames();
//
//            for (int i = 0; i < args.length; i++) {
//                if (i > 0) paramsBuilder.append("; ");
//
//                String paramName = i < paramNames.length ? paramNames[i] : "arg" + i;
//                Object paramValue = args[i];
//
//                paramsBuilder.append(paramName).append(": ");
//
//                if (paramValue == null) {
//                    paramsBuilder.append("null");
//                } else {
//                    // 处理不同类型的参数
//                    paramsBuilder.append(formatParameterValue(paramValue));
//                }
//            }
//
//            return paramsBuilder.toString();
//        } catch (Exception e) {
//            log.warn("提取方法参数失败: {}", e.getMessage());
//            return "参数提取失败";
//        }
//    }
//
//    /**
//     * 格式化参数值 - 新增的方法
//     */
//    private String formatParameterValue(Object value) {
//        if (value == null) {
//            return "null";
//        }
//
//        Class<?> clazz = value.getClass();
//
//        // 处理数组类型
//        if (clazz.isArray()) {
//            return formatArrayValue(value);
//        }
//
//        // 处理字符串类型
//        if (value instanceof String) {
//            String str = (String) value;
//            if (str.length() > 100) {
//                return "\"" + str.substring(0, 100) + "...\"";
//            }
//            return "\"" + str + "\"";
//        }
//
//        // 处理常见包装类型
//        if (value instanceof Integer || value instanceof Long ||
//                value instanceof Double || value instanceof Float ||
//                value instanceof Boolean || value instanceof Character ||
//                value instanceof Byte || value instanceof Short) {
//            return value.toString();
//        }
//
//        // 处理集合类型
//        if (value instanceof java.util.Collection) {
//            java.util.Collection<?> collection = (java.util.Collection<?>) value;
//            return "集合[大小=" + collection.size() + "]";
//        }
//
//        // 处理Map类型
//        if (value instanceof java.util.Map) {
//            java.util.Map<?, ?> map = (java.util.Map<?, ?>) value;
//            return "Map[大小=" + map.size() + "]";
//        }
//
//        // 尝试判断是否为WmsTask类型
//        if (clazz.getName().contains("WmsTask")) {
//            return formatWmsTaskValue(value);
//        }
//
//        // 其他类型，使用简化显示
//        String strValue = value.toString();
//        if (strValue.startsWith(clazz.getName() + "@")) {
//            // 如果是默认的toString()，只显示类名
//            return clazz.getSimpleName();
//        }
//
//        if (strValue.length() > 150) {
//            return strValue.substring(0, 150) + "...";
//        }
//
//        return strValue;
//    }
//
//    /**
//     * 格式化数组值 - 新增的方法
//     */
//    private String formatArrayValue(Object array) {
//        if (array == null) {
//            return "null";
//        }
//
//        Class<?> componentType = array.getClass().getComponentType();
//
//        // 基本类型数组
//        if (componentType.isPrimitive()) {
//            if (componentType == int.class) {
//                return "int数组[长度=" + ((int[]) array).length + "]";
//            } else if (componentType == long.class) {
//                return "long数组[长度=" + ((long[]) array).length + "]";
//            } else if (componentType == double.class) {
//                return "double数组[长度=" + ((double[]) array).length + "]";
//            } else if (componentType == float.class) {
//                return "float数组[长度=" + ((float[]) array).length + "]";
//            } else if (componentType == boolean.class) {
//                return "boolean数组[长度=" + ((boolean[]) array).length + "]";
//            } else if (componentType == byte.class) {
//                return "byte数组[长度=" + ((byte[]) array).length + "]";
//            } else if (componentType == short.class) {
//                return "short数组[长度=" + ((short[]) array).length + "]";
//            } else if (componentType == char.class) {
//                return "char数组[长度=" + ((char[]) array).length + "]";
//            }
//        }
//
//        // 对象数组
//        Object[] objArray = (Object[]) array;
//        if (objArray.length == 0) {
//            return "空数组";
//        }
//
//        // 判断是否为WmsTask数组
//        if (objArray.length > 0 && objArray[0] != null &&
//                objArray[0].getClass().getName().contains("WmsTask")) {
//            return formatWmsTaskArray(objArray);
//        }
//
//        return componentType.getSimpleName() + "数组[长度=" + objArray.length + "]";
//    }
//
//    /**
//     * 格式化WmsTask数组 - 新增的方法
//     */
//    private String formatWmsTaskArray(Object[] wmsTaskArray) {
//        if (wmsTaskArray.length == 0) {
//            return "WmsTask空数组";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("WmsTask数组[长度=").append(wmsTaskArray.length).append(", 内容=[");
//
//        // 只显示前3个任务的信息
//        int displayCount = Math.min(wmsTaskArray.length, 3);
//        for (int i = 0; i < displayCount; i++) {
//            if (i > 0) sb.append(", ");
//
//            Object task = wmsTaskArray[i];
//            if (task == null) {
//                sb.append("null");
//            } else {
//                sb.append(formatWmsTaskValue(task));
//            }
//        }
//
//        if (wmsTaskArray.length > displayCount) {
//            sb.append(", ... 还有").append(wmsTaskArray.length - displayCount).append("个");
//        }
//
//        sb.append("]]");
//        return sb.toString();
//    }
//
//    /**
//     * 格式化WmsTask对象 - 新增的方法
//     */
//    private String formatWmsTaskValue(Object wmsTask) {
//        try {
//            // 使用反射获取WmsTask的关键字段
//            Class<?> clazz = wmsTask.getClass();
//
//            // 尝试获取常见的方法
//            java.lang.reflect.Method getWmsIdMethod = clazz.getMethod("getWmsId");
//            java.lang.reflect.Method getFromLocationMethod = null;
//            java.lang.reflect.Method getToLocationMethod = null;
//
//            try {
//                getFromLocationMethod = clazz.getMethod("getFromLocation");
//            } catch (NoSuchMethodException e) {
//                // 方法可能不存在，继续尝试其他方法名
//            }
//
//            try {
//                getToLocationMethod = clazz.getMethod("getToLocation");
//            } catch (NoSuchMethodException e) {
//                // 方法可能不存在
//            }
//
//            StringBuilder info = new StringBuilder("WmsTask{");
//
//            // 获取wmsId
//            Object wmsId = getWmsIdMethod.invoke(wmsTask);
//            if (wmsId != null) {
//                info.append("wmsId=").append(wmsId);
//            }
//
//            // 获取fromLocation
//            if (getFromLocationMethod != null) {
//                Object fromLocation = getFromLocationMethod.invoke(wmsTask);
//                if (fromLocation != null) {
//                    if (info.length() > "WmsTask{".length()) info.append(", ");
//                    info.append("from=").append(fromLocation);
//                }
//            }
//
//            // 获取toLocation
//            if (getToLocationMethod != null) {
//                Object toLocation = getToLocationMethod.invoke(wmsTask);
//                if (toLocation != null) {
//                    if (info.length() > "WmsTask{".length()) info.append(", ");
//                    info.append("to=").append(toLocation);
//                }
//            }
//
//            info.append("}");
//            return info.toString();
//
//        } catch (Exception e) {
//            // 如果反射失败，返回简化信息
//            return "WmsTask对象";
//        }
//    }
//
//    /**
//     * Controller返回后通知
//     */
//    @AfterReturning(pointcut = "wmsControllerPointcut() || tesControllerPointcut() || rcsControllerPointcut()",
//            returning = "result")
//    public void doAfterReturning(Object result) {
//        try {
//            SystemLog systemLog = logThreadLocal.get();
//            if (systemLog != null) {
//                Long endTime = System.currentTimeMillis();
//                Long startTime = startTimeThreadLocal.get();
//
//                systemLog.setLevel("INFO");
//                systemLog.setMessage(systemLog.getMessage() + " - 成功");
//
//                // 更新details包含执行时间
//                String details = systemLog.getDetails() + String.format(" | 执行时间: %dms", (endTime - startTime));
//                systemLog.setDetails(details);
//
//                // 异步保存到数据库
//                asyncSaveLog(systemLog);
//            }
//        } catch (Exception e) {
//            log.error("记录返回日志异常: {}", e.getMessage());
//        } finally {
//            cleanUp();
//        }
//    }
//
//    /**
//     * Controller异常通知
//     */
//    @AfterThrowing(pointcut = "wmsControllerPointcut() || tesControllerPointcut() || rcsControllerPointcut()",
//            throwing = "e")
//    public void doAfterThrowing(Throwable e) {
//        try {
//            SystemLog systemLog = logThreadLocal.get();
//            if (systemLog != null) {
//                Long endTime = System.currentTimeMillis();
//                Long startTime = startTimeThreadLocal.get();
//
//                systemLog.setLevel("ERROR");
//                systemLog.setMessage(systemLog.getMessage() + " - 失败: " + e.getMessage());
//
//                // 更新details包含异常信息和执行时间
//                String details = systemLog.getDetails() +
//                        String.format(" | 执行时间: %dms | 异常: %s",
//                                (endTime - startTime), getExceptionMessage(e));
//                systemLog.setDetails(details);
//
//                // 异步保存到数据库
//                asyncSaveLog(systemLog);
//            }
//        } catch (Exception ex) {
//            log.error("记录异常日志异常: {}", ex.getMessage());
//        } finally {
//            cleanUp();
//        }
//    }
//
//    /**
//     * 数据库操作通知
//     */
//    @AfterReturning("mapperPointcut()")
//    public void doAfterMapper(JoinPoint joinPoint) {
//        try {
//            SystemLog systemLog = new SystemLog();
//            systemLog.setUsertype("wcs-api");
//            systemLog.setLogtype("DataBase");
//            systemLog.setLevel("INFO");
//            systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//            systemLog.setIsarchived("0");
//            systemLog.setUserid("");
//
//            // 设置模块和操作
//            systemLog.setModule("DATA_ACCESS");
//            systemLog.setOperation(joinPoint.getSignature().getName());
//
//            // 构建消息
//            String methodName = joinPoint.getSignature().getName();
//            systemLog.setMessage("数据库操作: " + methodName);
//            systemLog.setDetails("执行方法: " + joinPoint.getSignature().toShortString());
//
//            // 异步保存
//            asyncSaveLog(systemLog);
//
//        } catch (Exception e) {
//            log.error("记录数据库操作日志异常: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * Service层重要操作通知
//     */
//    @AfterReturning("servicePointcut()")
//    public void doAfterService(JoinPoint joinPoint) {
//        try {
//            String methodName = joinPoint.getSignature().getName();
//
//            // 只记录重要的服务方法
//            if (isImportantServiceMethod(methodName)) {
//                SystemLog systemLog = new SystemLog();
//                systemLog.setUsertype("wcs-api");
//                systemLog.setLogtype("System");
//                systemLog.setLevel("INFO");
//                systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                systemLog.setIsarchived("0");
//                systemLog.setUserid("");
//
//                systemLog.setModule("SERVICE");
//                systemLog.setOperation(methodName);
//                systemLog.setMessage("服务操作: " + methodName);
//                systemLog.setDetails("执行方法: " + joinPoint.getSignature().toShortString());
//
//                asyncSaveLog(systemLog);
//            }
//        } catch (Exception e) {
//            log.error("记录服务操作日志异常: {}", e.getMessage());
//        }
//    }
//
//    // ========== 工具方法 ==========
//
//    /**
//     * 异步保存日志到数据库
//     */
//    @Async("logExecutor")
//    public void asyncSaveLog(SystemLog systemLog) {
//        try {
//            systemLogMapper.add(systemLog);
//            log.trace("日志保存成功: {}", systemLog.getMessage());
//        } catch (Exception e) {
//            log.error("保存日志到数据库失败: {}", e.getMessage());
//            // 降级策略：可以记录到文件或发送到消息队列
//        }
//    }
//
//    /**
//     * 检查返回结果是否成功
//     */
//    private boolean checkResultIsSuccess(Object result) {
//        if (result == null) {
//            return false;
//        }
//
//        // 检查是否是TaskResqon类型
//        if (result instanceof TaskResqon) {
//            TaskResqon taskResqon = (TaskResqon) result;
//            // 假设returnCode为0表示成功
//            return taskResqon.getReturnCode() == 0;
//        }
//
//        // 检查是否是TesTaskCancelResqon类型
//        if (result instanceof TesTaskCancelResqon) {
//            TesTaskCancelResqon cancelResqon = (TesTaskCancelResqon) result;
//            // 假设returnCode为0表示成功
//            return cancelResqon.getReturnCode().equals("200");
//        }
//
//        // 检查是否是ReleaseStationResqon类型
//        if (result instanceof ReleaseStationResqon) {
//            ReleaseStationResqon stationResqon = (ReleaseStationResqon) result;
//            // 假设returnCode为0表示成功
//            return stationResqon.getReturnCode() == 0;
//        }
//
//        // 其他类型默认认为成功
//        return true;
//    }
//
//    /**
//     * 从返回结果中提取错误信息
//     */
//    private String extractErrorMessageFromResult(Object result) {
//        if (result == null) {
//            return "返回结果为null";
//        }
//
//        if (result instanceof TaskResqon) {
//            TaskResqon taskResqon = (TaskResqon) result;
//            return String.format("Code: %d, Msg: %s, UserMsg: %s",
//                    taskResqon.getReturnCode(),
//                    taskResqon.getReturnMsg(),
//                    taskResqon.getReturnUserMsg());
//        }
//
//        if (result instanceof TesTaskCancelResqon) {
//            TesTaskCancelResqon cancelResqon = (TesTaskCancelResqon) result;
//            return String.format("Code: %d, Msg: %s",
//                    cancelResqon.getReturnCode(),
//                    cancelResqon.getReturnMsg());
//        }
//
//        if (result instanceof ReleaseStationResqon) {
//            ReleaseStationResqon stationResqon = (ReleaseStationResqon) result;
//            return String.format("Code: %d, Msg: %s",
//                    stationResqon.getReturnCode(),
//                    stationResqon.getReturnMsg());
//        }
//
//        return "未知错误类型";
//    }
//
//    /**
//     * 确定Controller的logtype
//     */
//    private String determineLogType(JoinPoint joinPoint) {
//        String className = joinPoint.getSignature().getDeclaringTypeName();
//
//        if (className.contains("WmsController")) {
//            return "WMS";
//        } else if (className.contains("TesController")) {
//            return "Tes";
//        } else if (className.contains("RcsController")) {
//            return "Rcs";
//        }
//
//        return "System";
//    }
//
//    /**
//     * 确定HTTP Service的logtype
//     */
//    private String determineHttpLogType(ProceedingJoinPoint point) {
//        String className = point.getSignature().getDeclaringTypeName();
//
//        if (className.contains("TesHttpService")) {
//            return "TesHttp";
//        } else if (className.contains("WMSHttpService")) {
//            return "WMSHttp";
//        } else if (className.contains("RcsHttpService")) {
//            return "RcsHttp";
//        }
//
//        return "Http";
//    }
//
//    /**
//     * 构建请求消息
//     */
//    private String buildRequestMessage(HttpServletRequest request, JoinPoint joinPoint) {
//        String method = request.getMethod();
//        String url = request.getRequestURL().toString();
//        String controllerType = determineLogType(joinPoint);
//
//        return String.format("%s请求 - %s: %s", controllerType, method, url);
//    }
//
//    /**
//     * 构建请求详情
//     */
//    private String buildRequestDetails(HttpServletRequest request, JoinPoint joinPoint) {
//        StringBuilder details = new StringBuilder();
//
//        // 方法信息
//        details.append("方法: ").append(joinPoint.getSignature().toShortString());
//
//        // 请求参数
//        Map<String, String[]> params = request.getParameterMap();
//        if (!params.isEmpty()) {
//            details.append(" | 参数: ");
//            params.forEach((key, values) -> {
//                if (!isSensitiveParam(key)) {
//                    details.append(key).append("=").append(Arrays.toString(values)).append("; ");
//                } else {
//                    details.append(key).append("=***; ");
//                }
//            });
//        }
//
//        return details.toString();
//    }
//
//    /**
//     * 提取HTTP URL
//     */
//    private String extractHttpUrl(ProceedingJoinPoint point) {
//        Object[] args = point.getArgs();
//        if (args != null && args.length > 0 && args[0] instanceof String) {
//            return (String) args[0];
//        }
//        return "Unknown URL";
//    }
//
//    /**
//     * 提取模块和操作信息
//     */
//    private void extractModuleAndOperation(SystemLog systemLog, JoinPoint joinPoint) {
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Method method = signature.getMethod();
//
//        // 模块基于包路径
//        String className = signature.getDeclaringTypeName();
//        if (className.contains(".controller.")) {
//            systemLog.setModule("CONTROLLER");
//        } else if (className.contains(".service.")) {
//            systemLog.setModule("SERVICE");
//        } else {
//            systemLog.setModule("OTHER");
//        }
//
//        // 操作描述
//        systemLog.setOperation(method.getName());
//
//        // 可以从注解中提取更详细的描述
//        try {
//            if (method.isAnnotationPresent(RequestMapping.class)) {
//                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
//                if (mapping.value().length > 0) {
//                    systemLog.setOperation(Arrays.toString(mapping.value()));
//                }
//            }
//        } catch (Exception e) {
//            // 忽略注解解析异常
//        }
//    }
//
//    /**
//     * 判断是否为敏感参数
//     */
//    private boolean isSensitiveParam(String paramName) {
//        String[] sensitiveKeywords = {"password", "pwd", "secret", "token", "key"};
//        String lowerParamName = paramName.toLowerCase();
//        for (String keyword : sensitiveKeywords) {
//            if (lowerParamName.contains(keyword)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 判断是否为重要的服务方法
//     */
//    private boolean isImportantServiceMethod(String methodName) {
//        String[] importantPrefixes = {"save", "update", "delete", "create", "process", "handle"};
//        for (String prefix : importantPrefixes) {
//            if (methodName.startsWith(prefix)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 获取客户端IP地址
//     */
//    private String getClientIpAddress(HttpServletRequest request) {
//        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP"};
//
//        for (String header : headers) {
//            String ip = request.getHeader(header);
//            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
//                return ip.split(",")[0].trim();
//            }
//        }
//        return request.getRemoteAddr();
//    }
//
//    /**
//     * 获取异常消息
//     */
//    private String getExceptionMessage(Throwable e) {
//        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
//    }
//
//    /**
//     * 清理ThreadLocal
//     */
//    private void cleanUp() {
//        logThreadLocal.remove();
//        startTimeThreadLocal.remove();
//    }
//}
package com.kskj.until;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kskj.mapper.SystemLogMapper;
import com.kskj.pojo.SystemLog;
import com.kskj.pojo.TES.ReleaseStationResqon;
import com.kskj.pojo.TES.TaskResqon;
import com.kskj.pojo.TES.TesTaskCancelResqon;
import com.kskj.pojo.WMS.WmsLocationResqon;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Aspect
@Component
@Slf4j
public class SystemLogAspect {
    @Autowired
    private SystemLogMapper systemLogMapper;

    // 使用Jackson ObjectMapper - 线程安全
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        objectMapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
    }

    private ThreadLocal<SystemLog> logThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();
    private ThreadLocal<String> requestArgsThreadLocal = new ThreadLocal<>();
    private ThreadLocal<AtomicBoolean> logSavedThreadLocal = new ThreadLocal<>();

    // Controller切点定义
    @Pointcut("execution(* com.kskj.controller.WmsController.*(..))")
    public void wmsControllerPointcut() {}

    @Pointcut("execution(* com.kskj.controller.TesController.*(..))")
    public void tesControllerPointcut() {}

    @Pointcut("execution(* com.kskj.controller.RcsController.*(..))")
    public void rcsControllerPointcut() {}

    // Service切点定义
    @Pointcut("execution(* com.kskj.service..*.*(..))")
    public void servicePointcut() {}

    // 第三方HTTP调用切点
    @Pointcut("execution(* com.kskj.HttpService..*.*(..))")
    public void httpServicePointcut() {}

    @Pointcut("execution(* com.kskj.mapper..*.*(..)) && " +
            "!execution(* com.kskj.mapper.SystemLogMapper.add(..)) && " +
            "!execution(* com.kskj.mapper.TaskMapper.findByTaskStatusAndTaskType(..)) && " +
            "!execution(* com.kskj.mapper.RetryMechanismMapper.findAll(..))&&"+
            "!execution(* com.kskj.mapper.TaskMapper.findByTaskStatus(..))" )
    public void mapperPointcut() {}

    /**
     * Controller层前置通知
     */
    @Before("wmsControllerPointcut() || tesControllerPointcut() || rcsControllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        try {
            Long startTime = System.currentTimeMillis();
            startTimeThreadLocal.set(startTime);
            logSavedThreadLocal.set(new AtomicBoolean(false));

            SystemLog systemLog = new SystemLog();
            systemLog.setUsertype("wcs-api");
            systemLog.setLevel("INFO");
            systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            systemLog.setIsarchived("0");
            systemLog.setUserid("");

            String logtype = determineLogType(joinPoint);
            systemLog.setLogtype(logtype);

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                systemLog.setIpaddress(getClientIpAddress(request));

                String message = buildRequestMessage(request, joinPoint);
                systemLog.setMessage(message);

                String details = buildRequestDetails(request, joinPoint);
                String methodParams = extractMethodParametersSafely(joinPoint);
                if (!methodParams.isEmpty()) {
                    details += " | 方法参数: " + methodParams;
                }
                systemLog.setDetails(details);
            }

            extractModuleAndOperation(systemLog, joinPoint);
            logThreadLocal.set(systemLog);

        } catch (Exception e) {
            log.error("记录请求日志前置处理异常: {}", e.getMessage());
        }
    }

    /**
     * HTTP服务调用环绕通知 - 增强版本
     */
    @Around("httpServicePointcut()")
    public Object aroundHttpService(ProceedingJoinPoint point) throws Throwable {
        Long startTime = System.currentTimeMillis();
        String logtype = determineHttpLogType(point);
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();

        // 记录请求参数
        String requestParams = buildHttpRequestParams(point);
        String url = extractHttpUrl(point, point.getArgs(), methodName);

        requestArgsThreadLocal.set(requestParams);

        try {
            Object result = point.proceed();
            Long endTime = System.currentTimeMillis();
            Long costTime = endTime - startTime;

            boolean isSuccess = checkHttpResultIsSuccess(result);
            String responseParams = buildResponseParams(result);

            if (isSuccess) {
                createAndSaveHttpLog(logtype, "INFO", "HTTP调用成功",
                        url, methodName, costTime, null, requestParams, responseParams, className);
            } else {
                String errorMsg = extractErrorMessageFromResult(result);
                createAndSaveHttpLog(logtype, "WARN", "HTTP调用返回失败",
                        url, methodName, costTime, errorMsg, requestParams, responseParams, className);
            }

            return result;
        } catch (Exception e) {
            Long endTime = System.currentTimeMillis();
            Long costTime = endTime - startTime;
            String requestParamsOnError = requestArgsThreadLocal.get();

            String errorMsg = getFullExceptionMessage(e);
            createAndSaveHttpLog(logtype, "ERROR", "HTTP调用异常",
                    url, methodName, costTime, errorMsg, requestParamsOnError, "N/A - 调用异常", className);

            log.error("HTTP调用异常 - {}.{} 耗时: {}ms, URL: {}, 异常: {}",
                    className, methodName, costTime, url, errorMsg, e);

            throw e;
        } finally {
            requestArgsThreadLocal.remove();
        }
    }

    /**
     * 提取HTTP URL
     */
    private String extractHttpUrl(ProceedingJoinPoint point, Object[] args, String methodName) {
        try {
            for (Object arg : args) {
                if (arg instanceof String) {
                    String url = (String) arg;
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        return url;
                    }
                }
            }

            Object target = point.getTarget();
            try {
                java.lang.reflect.Field urlField = target.getClass().getDeclaredField("url2");
                urlField.setAccessible(true);
                Object urlValue = urlField.get(target);
                if (urlValue instanceof String) {
                    return (String) urlValue;
                }
            } catch (NoSuchFieldException e) {
                String[] possibleUrlFields = {"url", "url2", "baseUrl", "serviceUrl"};
                for (String fieldName : possibleUrlFields) {
                    try {
                        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(target);
                        if (value instanceof String) {
                            return (String) value;
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取HTTP URL失败: {}", e.getMessage());
        }

        return methodName + " (URL未知)";
    }

    /**
     * 检查HTTP调用结果是否成功
     */
    private boolean checkHttpResultIsSuccess(Object result) {
        if (result == null) {
            return false;
        }

        if (result instanceof TaskResqon) {
            TaskResqon taskResqon = (TaskResqon) result;
            return taskResqon.getReturnCode() == 0;
        }

        if (result instanceof TesTaskCancelResqon) {
            TesTaskCancelResqon cancelResqon = (TesTaskCancelResqon) result;
            return "200".equals(cancelResqon.getReturnCode());
        }

        if (result instanceof ReleaseStationResqon) {
            ReleaseStationResqon stationResqon = (ReleaseStationResqon) result;
            return stationResqon.getReturnCode() == 0;
        }

        if (result instanceof WmsLocationResqon) {
            WmsLocationResqon locationResqon = (WmsLocationResqon) result;
            try {
                java.lang.reflect.Method getSuccessMethod = result.getClass().getMethod("getSuccess");
                Object successValue = getSuccessMethod.invoke(result);
                if (successValue instanceof Boolean) {
                    return (Boolean) successValue;
                }
            } catch (Exception e) {
            }

            try {
                java.lang.reflect.Method getCodeMethod = result.getClass().getMethod("getCode");
                Object codeValue = getCodeMethod.invoke(result);
                if (codeValue instanceof Integer) {
                    return (Integer) codeValue == 200 || (Integer) codeValue == 0;
                } else if (codeValue instanceof String) {
                    String code = (String) codeValue;
                    return "200".equals(code) || "0".equals(code) || "success".equalsIgnoreCase(code);
                }
            } catch (Exception e) {
            }
        }

        try {
            Class<?> clazz = result.getClass();

            try {
                java.lang.reflect.Method getSuccessMethod = clazz.getMethod("isSuccess");
                Object success = getSuccessMethod.invoke(result);
                if (success instanceof Boolean) {
                    return (Boolean) success;
                }
            } catch (NoSuchMethodException e) {
                try {
                    java.lang.reflect.Method getSuccessMethod = clazz.getMethod("getSuccess");
                    Object success = getSuccessMethod.invoke(result);
                    if (success instanceof Boolean) {
                        return (Boolean) success;
                    }
                } catch (NoSuchMethodException e2) {
                }
            }

            try {
                java.lang.reflect.Method getCodeMethod = clazz.getMethod("getCode");
                Object code = getCodeMethod.invoke(result);
                if (code instanceof Integer) {
                    return (Integer) code == 200 || (Integer) code == 0;
                } else if (code instanceof String) {
                    String codeStr = (String) code;
                    return "200".equals(codeStr) || "0".equals(codeStr) || "success".equalsIgnoreCase(codeStr);
                }
            } catch (NoSuchMethodException e) {
            }

            try {
                java.lang.reflect.Method getStatusMethod = clazz.getMethod("getStatus");
                Object status = getStatusMethod.invoke(result);
                if (status instanceof String) {
                    String statusStr = (String) status;
                    return "success".equalsIgnoreCase(statusStr) || "ok".equalsIgnoreCase(statusStr);
                }
            } catch (NoSuchMethodException e) {
            }
        } catch (Exception e) {
            log.debug("检查结果状态失败: {}", e.getMessage());
        }

        return true;
    }

    /**
     * 获取完整的异常信息
     */
    private String getFullExceptionMessage(Throwable e) {
        if (e == null) return "未知异常";

        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage());

        Throwable cause = e.getCause();
        if (cause != null) {
            sb.append(" | 根因: ").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
        }

        return sb.toString();
    }

    /**
     * 创建并保存HTTP调用日志
     */
    private void createAndSaveHttpLog(String logtype, String level, String message,
                                      String url, String method, Long costTime,
                                      String errorMsg, String requestParams, String responseParams,
                                      String className) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUsertype("wcs-api");
            systemLog.setLogtype(logtype);
            systemLog.setLevel(level);
            systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            systemLog.setIsarchived("0");
            systemLog.setUserid("");
            systemLog.setModule("HTTP_CLIENT");
            systemLog.setOperation(className + "." + method);

            String fullMessage = String.format("%s: %s.%s - %s", message, className, method, url);
            systemLog.setMessage(fullMessage);

            StringBuilder details = new StringBuilder();
            details.append("类名: ").append(className);
            details.append(" | 方法: ").append(method);
            details.append(" | URL: ").append(url != null ? url : "N/A");

            if (costTime != null) {
                details.append(String.format(" | 耗时: %dms", costTime));
            }

            if (requestParams != null && !requestParams.isEmpty()) {
                details.append(" | 请求参数: ").append(requestParams.length() > 500 ?
                        requestParams.substring(0, 500) + "..." : requestParams);
            }

            if (responseParams != null && !responseParams.isEmpty()) {
                details.append(" | 响应结果: ").append(responseParams.length() > 500 ?
                        responseParams.substring(0, 500) + "..." : responseParams);
            }

            if (errorMsg != null) {
                details.append(" | 错误信息: ").append(errorMsg);
            }

            systemLog.setDetails(details.toString());

            syncSaveLogWithRetry(systemLog);

        } catch (Exception e) {
            log.error("创建HTTP调用日志失败: {}", e.getMessage());
        }
    }

    /**
     * 同步保存日志，带重试机制
     */
    private void syncSaveLogWithRetry(SystemLog systemLog) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                systemLogMapper.add(systemLog);
                return;
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    log.error("日志保存失败(重试{}次后): {}, 错误: {}", maxRetries, systemLog.getMessage(), e.getMessage());
                    asyncSaveLog(systemLog);
                } else {
                    try {
                        Thread.sleep(100 * (i + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * 异步保存日志到数据库
     */
    @Async("logExecutor")
    public void asyncSaveLog(SystemLog systemLog) {
        try {
            systemLogMapper.add(systemLog);
        } catch (Exception e) {
            log.error("异步保存日志到数据库失败: {}, 日志内容: {}", e.getMessage(),
                    systemLog != null ? systemLog.getMessage() : "null");
        }
    }

    /**
     * Controller返回后通知
     */
    @AfterReturning(pointcut = "wmsControllerPointcut() || tesControllerPointcut() || rcsControllerPointcut()",
            returning = "result")
    public void doAfterReturning(Object result) {
        try {
            SystemLog systemLog = logThreadLocal.get();
            AtomicBoolean logSaved = logSavedThreadLocal.get();

            if (systemLog != null && (logSaved == null || !logSaved.get())) {
                Long endTime = System.currentTimeMillis();
                Long startTime = startTimeThreadLocal.get();

                systemLog.setLevel("INFO");
                systemLog.setMessage(systemLog.getMessage() + " - 成功");

                String details = systemLog.getDetails() +
                        String.format(" | 执行时间: %dms", (endTime - startTime));
                systemLog.setDetails(details);

                syncSaveLogWithRetry(systemLog);
                if (logSaved != null) {
                    logSaved.set(true);
                }
            }
        } catch (Exception e) {
            log.error("记录返回日志异常: {}", e.getMessage());
        } finally {
            cleanUp();
        }
    }

    /**
     * Controller异常通知
     */
    @AfterThrowing(pointcut = "wmsControllerPointcut() || tesControllerPointcut() || rcsControllerPointcut()",
            throwing = "e")
    public void doAfterThrowing(Throwable e) {
        try {
            SystemLog systemLog = logThreadLocal.get();
            AtomicBoolean logSaved = logSavedThreadLocal.get();

            if (systemLog != null && (logSaved == null || !logSaved.get())) {
                Long endTime = System.currentTimeMillis();
                Long startTime = startTimeThreadLocal.get();

                systemLog.setLevel("ERROR");
                systemLog.setMessage(systemLog.getMessage() + " - 失败: " + e.getMessage());

                String details = systemLog.getDetails() +
                        String.format(" | 执行时间: %dms | 异常: %s",
                                (endTime - startTime), getExceptionMessage(e));
                systemLog.setDetails(details);

                syncSaveLogWithRetry(systemLog);
                if (logSaved != null) {
                    logSaved.set(true);
                }
            }
        } catch (Exception ex) {
            log.error("记录异常日志异常: {}", ex.getMessage());
        } finally {
            cleanUp();
        }
    }

    /**
     * 数据库操作通知
     */
    @AfterReturning("mapperPointcut()")
    public void doAfterMapper(JoinPoint joinPoint) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUsertype("wcs-api");
            systemLog.setLogtype("DataBase");
            systemLog.setLevel("INFO");
            systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            systemLog.setIsarchived("0");
            systemLog.setUserid("");

            systemLog.setModule("DATA_ACCESS");
            systemLog.setOperation(joinPoint.getSignature().getName());

            String methodName = joinPoint.getSignature().getName();
            systemLog.setMessage("数据库操作: " + methodName);
            systemLog.setDetails("执行方法: " + joinPoint.getSignature().toShortString());

            asyncSaveLog(systemLog);

        } catch (Exception e) {
            log.error("记录数据库操作日志异常: {}", e.getMessage());
        }
    }

    /**
     * Service层重要操作通知
     */
    @AfterReturning("servicePointcut()")
    public void doAfterService(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();

            if (isImportantServiceMethod(methodName)) {
                SystemLog systemLog = new SystemLog();
                systemLog.setUsertype("wcs-api");
                systemLog.setLogtype("System");
                systemLog.setLevel("INFO");
                systemLog.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                systemLog.setIsarchived("0");
                systemLog.setUserid("");

                systemLog.setModule("SERVICE");
                systemLog.setOperation(methodName);
                systemLog.setMessage("服务操作: " + methodName);
                systemLog.setDetails("执行方法: " + joinPoint.getSignature().toShortString());

                asyncSaveLog(systemLog);
            }
        } catch (Exception e) {
            log.error("记录服务操作日志异常: {}", e.getMessage());
        }
    }

    /**
     * 安全的提取方法参数
     */
    private String extractMethodParametersSafely(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "无参数";
            }

            StringBuilder paramsBuilder = new StringBuilder();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();

            for (int i = 0; i < args.length; i++) {
                if (i > 0) paramsBuilder.append("; ");
                String paramName = i < paramNames.length ? paramNames[i] : "arg" + i;
                paramsBuilder.append(paramName).append(": ");
                paramsBuilder.append(objectToJsonSafe(args[i]));
            }

            return paramsBuilder.toString();
        } catch (Exception e) {
            log.warn("提取方法参数失败: {}", e.getMessage());
            return "参数提取失败";
        }
    }

    /**
     * 安全地将对象转换为JSON字符串
     */
    private String objectToJsonSafe(Object obj) {
        if (obj == null) return "null";

        Class<?> clazz = obj.getClass();

        if (isSimpleType(clazz)) {
            return simpleTypeToString(obj);
        }

        if (clazz.isArray()) {
            return arrayToJsonSafe(obj);
        }

        if (obj instanceof Iterable) {
            return iterableToJsonSafe((Iterable<?>) obj);
        }

        if (obj instanceof Map) {
            return mapToJsonSafe((Map<?, ?>) obj);
        }

        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.length() > 500 ? json.substring(0, 500) + "...[长度:" + json.length() + "]" : json;
        } catch (JsonProcessingException e) {
            log.debug("对象序列化失败: {}, 返回简化信息", e.getMessage());
            return clazz.getSimpleName() + "[id=" + System.identityHashCode(obj) + "]";
        } catch (Exception e) {
            return clazz.getSimpleName();
        }
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class ||
                clazz == java.util.Date.class ||
                clazz == java.time.LocalDateTime.class ||
                clazz == java.time.LocalDate.class ||
                clazz == java.time.LocalTime.class;
    }

    /**
     * 简单类型转字符串
     */
    private String simpleTypeToString(Object obj) {
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() > 100) {
                return "\"" + str.substring(0, 100) + "...\"[长度:" + str.length() + "]";
            }
            return "\"" + str + "\"";
        }
        return String.valueOf(obj);
    }

    /**
     * 数组安全转换为JSON
     */
    private String arrayToJsonSafe(Object array) {
        if (array == null) return "null";

        Class<?> componentType = array.getClass().getComponentType();

        if (componentType.isPrimitive()) {
            return array.toString();
        }

        Object[] objArray = (Object[]) array;
        if (objArray.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        int displayCount = Math.min(objArray.length, 3);

        for (int i = 0; i < displayCount; i++) {
            if (i > 0) sb.append(", ");
            sb.append(objectToJsonSafe(objArray[i]));
        }

        if (objArray.length > displayCount) {
            sb.append(", ... 还有").append(objArray.length - displayCount).append("个");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * 可迭代对象安全转换为JSON
     */
    private String iterableToJsonSafe(Iterable<?> iterable) {
        if (iterable == null) return "null";

        StringBuilder sb = new StringBuilder("[");
        int count = 0;
        int maxCount = 3;

        for (Object item : iterable) {
            if (count >= maxCount) {
                sb.append("...");
                break;
            }

            if (count > 0) sb.append(", ");
            sb.append(objectToJsonSafe(item));
            count++;
        }

        sb.append("]");
        if (count == 0) {
            return "[]";
        }

        return sb.toString();
    }

    /**
     * Map安全转换为JSON
     */
    private String mapToJsonSafe(Map<?, ?> map) {
        if (map == null) return "null";
        if (map.isEmpty()) return "{}";

        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        int maxCount = 3;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (count >= maxCount) {
                sb.append("...");
                break;
            }

            if (count > 0) sb.append(", ");
            sb.append(objectToJsonSafe(entry.getKey()))
                    .append(":")
                    .append(objectToJsonSafe(entry.getValue()));
            count++;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 构建HTTP请求参数字符串
     */
    private String buildHttpRequestParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            MethodSignature signature = (MethodSignature) point.getSignature();
            Parameter[] parameters = signature.getMethod().getParameters();

            StringBuilder paramsBuilder = new StringBuilder();

            for (int i = 0; i < args.length; i++) {
                if (i > 0) paramsBuilder.append("; ");

                String paramName = parameters.length > i ? parameters[i].getName() : "arg" + i;
                Object paramValue = args[i];

                paramsBuilder.append(paramName).append(": ");
                paramsBuilder.append(objectToJsonSafe(paramValue));
            }

            return paramsBuilder.toString();
        } catch (Exception e) {
            log.warn("构建HTTP请求参数失败: {}", e.getMessage());
            return "参数解析失败: " + e.getMessage();
        }
    }

    /**
     * 构建响应参数字符串
     */
    private String buildResponseParams(Object result) {
        if (result == null) {
            return "null";
        }

        try {
            return objectToJsonSafe(result);
        } catch (Exception e) {
            return "结果解析失败: " + e.getMessage();
        }
    }

    /**
     * 从返回结果中提取错误信息
     */
    private String extractErrorMessageFromResult(Object result) {
        if (result == null) {
            return "返回结果为null";
        }

        if (result instanceof TaskResqon) {
            TaskResqon taskResqon = (TaskResqon) result;
            return String.format("Code: %d, Msg: %s, UserMsg: %s",
                    taskResqon.getReturnCode(),
                    taskResqon.getReturnMsg(),
                    taskResqon.getReturnUserMsg());
        }

        if (result instanceof TesTaskCancelResqon) {
            TesTaskCancelResqon cancelResqon = (TesTaskCancelResqon) result;
            return String.format("Code: %s, Msg: %s",
                    cancelResqon.getReturnCode(),
                    cancelResqon.getReturnMsg());
        }

        if (result instanceof ReleaseStationResqon) {
            ReleaseStationResqon stationResqon = (ReleaseStationResqon) result;
            return String.format("Code: %d, Msg: %s",
                    stationResqon.getReturnCode(),
                    stationResqon.getReturnMsg());
        }

        return "未知错误类型";
    }

    /**
     * 确定Controller的logtype
     */
    private String determineLogType(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();

        if (className.contains("WmsController")) {
            return "WMS";
        } else if (className.contains("TesController")) {
            return "Tes";
        } else if (className.contains("RcsController")) {
            return "Rcs";
        }

        return "System";
    }

    /**
     * 确定HTTP Service的logtype
     */
    private String determineHttpLogType(ProceedingJoinPoint point) {
        String className = point.getSignature().getDeclaringTypeName();

        if (className.contains("TesHttpService")) {
            return "TesHttp";
        } else if (className.contains("WMSHttpService")) {
            return "WMSHttp";
        } else if (className.contains("RcsHttpService")) {
            return "RcsHttp";
        }

        return "Http";
    }

    /**
     * 构建请求消息
     */
    private String buildRequestMessage(HttpServletRequest request, JoinPoint joinPoint) {
        String method = request.getMethod();
        String url = request.getRequestURL().toString();
        String controllerType = determineLogType(joinPoint);

        return String.format("%s请求 - %s: %s", controllerType, method, url);
    }

    /**
     * 构建请求详情
     */
    private String buildRequestDetails(HttpServletRequest request, JoinPoint joinPoint) {
        StringBuilder details = new StringBuilder();

        details.append("方法: ").append(joinPoint.getSignature().toShortString());

        Map<String, String[]> params = request.getParameterMap();
        if (!params.isEmpty()) {
            details.append(" | 参数: ");
            params.forEach((key, values) -> {
                if (!isSensitiveParam(key)) {
                    details.append(key).append("=").append(Arrays.toString(values)).append("; ");
                } else {
                    details.append(key).append("=***; ");
                }
            });
        }

        return details.toString();
    }

    /**
     * 提取模块和操作信息
     */
    private void extractModuleAndOperation(SystemLog systemLog, JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String className = signature.getDeclaringTypeName();
        if (className.contains(".controller.")) {
            systemLog.setModule("CONTROLLER");
        } else if (className.contains(".service.")) {
            systemLog.setModule("SERVICE");
        } else {
            systemLog.setModule("OTHER");
        }

        systemLog.setOperation(method.getName());

        try {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                if (mapping.value().length > 0) {
                    systemLog.setOperation(Arrays.toString(mapping.value()));
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 判断是否为敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        String[] sensitiveKeywords = {"password", "pwd", "secret", "token", "key"};
        String lowerParamName = paramName.toLowerCase();
        for (String keyword : sensitiveKeywords) {
            if (lowerParamName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为重要的服务方法
     */
    private boolean isImportantServiceMethod(String methodName) {
        String[] importantPrefixes = {"save", "update", "delete", "create", "process", "handle"};
        for (String prefix : importantPrefixes) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP"};

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取异常消息
     */
    private String getExceptionMessage(Throwable e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    /**
     * 清理ThreadLocal
     */
    private void cleanUp() {
        logThreadLocal.remove();
        startTimeThreadLocal.remove();
        requestArgsThreadLocal.remove();
        logSavedThreadLocal.remove();
    }
}