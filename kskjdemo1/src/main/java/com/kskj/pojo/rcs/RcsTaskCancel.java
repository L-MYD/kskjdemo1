package com.kskj.pojo.rcs;

//此类为RCS任务取消的接口
public class RcsTaskCancel {
   private String robotlaskCode;//任务号，全局唯一
   private String cancelType;//任务取消类型 取消(原软取消) CANCEL   人工介入(原硬取消) DROP
   private String carrierCode;//回库的载具编号
   private String reason;//取消原因
   private String returnTasklype;//非必填
   private TargetRoute targetRoute;//非必填
   private Extra extra;//非必填

   public RcsTaskCancel() {
   }

   public RcsTaskCancel(String robotlaskCode, String cancelType, String carrierCode, String reason, String returnTasklype, TargetRoute targetRoute, Extra extra) {
      this.robotlaskCode = robotlaskCode;
      this.cancelType = cancelType;
      this.carrierCode = carrierCode;
      this.reason = reason;
      this.returnTasklype = returnTasklype;
      this.targetRoute = targetRoute;
      this.extra = extra;
   }

   public String getRobotlaskCode() {
      return robotlaskCode;
   }

   public void setRobotlaskCode(String robotlaskCode) {
      this.robotlaskCode = robotlaskCode;
   }

   public String getCancelType() {
      return cancelType;
   }

   public void setCancelType(String cancelType) {
      this.cancelType = cancelType;
   }

   public String getCarrierCode() {
      return carrierCode;
   }

   public void setCarrierCode(String carrierCode) {
      this.carrierCode = carrierCode;
   }

   public String getReason() {
      return reason;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public String getReturnTasklype() {
      return returnTasklype;
   }

   public void setReturnTasklype(String returnTasklype) {
      this.returnTasklype = returnTasklype;
   }

   public TargetRoute getTargetRoute() {
      return targetRoute;
   }

   public void setTargetRoute(TargetRoute targetRoute) {
      this.targetRoute = targetRoute;
   }

   public Extra getExtra() {
      return extra;
   }

   public void setExtra(Extra extra) {
      this.extra = extra;
   }
}
