package com.example.module_api;

import android.view.View;

public interface ViewInject<T> {
    /**
     *
     * @param target 所在的类
     * @param root 查找 View 的地方
     */
    void inject(T target, View root);
}
