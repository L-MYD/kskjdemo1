package com.kskj.pojo.rcs.reportertask;




public class RcsReporterTaskResonse {
 public String getCode() {
  return code;
 }

 public RcsReporterTaskResonse() {
 }

 public void setCode(String code) {
  this.code = code;
 }

 public String getMessage() {
  return message;
 }

 public void setMessage(String message) {
  this.message = message;
 }

 public RcsReporterTaskResonseData getData() {
  return data;
 }

 public void setData(RcsReporterTaskResonseData data) {
  this.data = data;
 }

 private String code;

 public RcsReporterTaskResonse(String code, String message, RcsReporterTaskResonseData data) {
  this.code = code;
  this.message = message;
  this.data = data;
 }

 private String message;
 private RcsReporterTaskResonseData data;
}
