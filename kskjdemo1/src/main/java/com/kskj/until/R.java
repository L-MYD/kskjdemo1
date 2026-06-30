package com.kskj.until;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author: Neuronet
 * @Date: 2023-08-04
 * @Description: 响应信息结果类
 * @Version:1.0
 */
public class R<T> {

    // 响应状态码 - 映射到C#的Code
    @JsonProperty("Code")
    private Integer code;

    // 操作结果 - C#类中没有这个字段，但我们可以保留，C#会忽略它
    private Boolean success;

    // 提示语 - 映射到C#的Message
    @JsonProperty("Message")
    private String mes;

    // 响应数据 - 映射到C#的data
    @JsonProperty("data")
    private T data;

    public R() {

    }

    public static <T> R<T> ok() {
        return new R<T>(200, true, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<T>(200, true, "操作成功", data);
    }

    public static <T> R<T> ok(String mes) {
        return new R<T>(200, true, mes, null);
    }

    public static <T> R<T> ok(String mes, T data) {
        return new R<T>(200, true, mes, data);
    }

    public static <T> R<T> fail() {
        return new R<T>(500, false, "操作失败！", null);
    }

    public static <T> R<T> fail(T data) {
        return new R<T>(500, false, "操作失败！", data);
    }

    public static <T> R<T> fail(String mes) {
        return new R<T>(500, false, mes, null);
    }

    public static <T> R<T> fail(Integer code) {
        return new R<T>(code, false, "操作失败！", null);
    }

    public static <T> R<T> fail(String mes, T data) {
        return new R<T>(500, false, mes, data);
    }

    public static <T> R<T> fail(Integer code, T data) {
        return new R<T>(code, false, "操作失败！", data);
    }

    public static <T> R<T> fail(Integer code, String mes) {
        return new R<T>(code, false, mes, null);
    }

    public static <T> R<T> fail(Integer code, String mes, T data) {
        return new R<T>(code, false, mes, data);
    }

    public R(Integer code, Boolean success, String mes, T data) {
        this.code = code;
        this.mes = mes;
        this.success = success;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}