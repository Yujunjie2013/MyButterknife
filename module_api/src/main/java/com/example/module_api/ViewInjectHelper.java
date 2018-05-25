package com.example.module_api;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.example.module_compiler.ProxyInfo;
import com.example.module_compiler.two.ProxyClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewInjectHelper {
    /**
     * 用来缓存反射出来的类，节省每次都去反射引起的性能问题,key为类的全名
     */
    private static final Map<String, Constructor<?>> BINDINGS = new LinkedHashMap<>();

    public static void inject(Activity taget) {
        inject(taget, taget.getWindow().getDecorView());
    }

    public static void inject(Activity host, View root) {
        String classFullName = host.getClass().getName() + ProxyClass.SUFFIX;
        //将所有构造器放入集合，每次先从集合中取，避免每次反射造成的性能问题。
        Constructor<?> constructor = BINDINGS.get(classFullName);
        try {
            if (constructor == null) {
                Class<?> aClass = Class.forName(classFullName);
                constructor = aClass.getDeclaredConstructor();
                BINDINGS.put(classFullName, constructor);
            }
            constructor.setAccessible(true);
            Object o = constructor.newInstance();
            if (o instanceof ViewInject) {
                ViewInject inject = (ViewInject) o;
                inject.inject(host, root);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static void inject(Activity o) {
//        inject(o, o.getWindow().getDecorView());
//    }
//
//    public static void inject(Activity host, View view) {
//        String className = host.getClass().getName() + ProxyInfo.ClassSuffix;
//        Constructor<?> constructor = BINDINGS.get(host.getClass());
//        try {
//            if (constructor == null) {
//                Class<?> aClass = Class.forName(className);
//                constructor = aClass.getDeclaredConstructor(host.getClass(), View.class);
//                BINDINGS.put(host.getClass(), constructor);
//            }
//            constructor.setAccessible(true);
//            constructor.newInstance(host, view);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
}
