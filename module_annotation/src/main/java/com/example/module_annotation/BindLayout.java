package com.example.module_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//获取布局id
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface BindLayout {
    int value();
}
