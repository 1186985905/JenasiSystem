package com.bx.imclient.annotation;

import com.bx.imcommon.enums.IMListenerType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 指定该注解可以用于类（ElementType.TYPE）和字段（ElementType.FIELD）上
@Target({ElementType.TYPE,ElementType.FIELD})
// 指定该注解在运行时仍然有效
@Retention(RetentionPolicy.RUNTIME)
// 标记该注解为一个Spring组件，可以被Spring框架管理
@Component
// 定义一个名为IMListener的自定义注解
public @interface IMListener {

    // 定义一个名为type的属性，类型为IMListenerType枚举，该属性不能为null
    IMListenerType type();

}
