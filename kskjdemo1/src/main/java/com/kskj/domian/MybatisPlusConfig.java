//package com.kskj.domian;
//
//import com.baomidou.mybatisplus.annotation.DbType;
//import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///*
// * MybatisPlus分页拦截器
// * @author rabbiter
// * @date 2023/1/2 20:06
// */
////@Configuration
////public class MybatisPlusConfig {
////    @Bean
////    public MybatisPlusInterceptor mybatisPlusInterceptor() {
////        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
////        // SQL Server 分页插件
////        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQL_SERVER));
////        return interceptor;
////    }
////}
//// 因为这是sql server 数据库，因此会和mysql的配置有点不一样，sql server版本不同，也会有些差别
//@Configuration
//public class MybatisPlusConfig {
//    @Bean
//    public MybatisPlusInterceptor mybatisPlusInterceptor(){
//        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
//        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQL_SERVER2005)); // 高版本使用SQL_SERVER
//        return mybatisPlusInterceptor;
//    }
//}
//
