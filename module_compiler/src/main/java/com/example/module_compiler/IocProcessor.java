package com.example.module_compiler;

import com.example.module_annotation.BindView;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

//@AutoService(Processor.class)
public class IocProcessor extends AbstractProcessor {

    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    //存放代理类的集合，key为代理类的全路劲
    private Map<String, ProxyInfo> mProxyMap = new HashMap<String, ProxyInfo>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();//跟文件相关的辅助类，生成JavaSourceCode.
        elementUtils = processingEnvironment.getElementUtils();//跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
        messager = processingEnvironment.getMessager();//跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
    }

    //返回支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> objects = new LinkedHashSet<>();
        //这里应该将所有的注解类型添加进去
        objects.add(BindView.class.getCanonicalName());
        return objects;
    }

    //返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Element
     * - VariableElement  一般代表成员变量
     * - ExecutableElement 一般代表类中的方法
     * - TypeElement     一般代表代表类
     * - PackageElement  一般代表Package
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectionInfo(roundEnvironment);
        generateClass();
        return true;
    }

    private void generateClass() {
        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                JavaFileObject file = filer.createSourceFile(proxyInfo.getProxyclassFullName(), proxyInfo.getTypeElement());
                Writer writer = file.openWriter();
                writer.write(proxyInfo.genrateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //收集生成类所需要的信息
    private void collectionInfo(RoundEnvironment roundEnvironment) {
        //因为process会执行多次，避免生成重复的代理类，避免生成类的类名已存在异常。所以先清理
        mProxyMap.clear();
        //返回使用给定注释类型注释的元素。(获得被该注解声明的类和变量)
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
//           检查element的类型
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                //类的完整路径
                String qualifedName = typeElement.getQualifiedName().toString();
                //类名
                String className = typeElement.getSimpleName().toString();
                //包名
                String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                BindView bindView = element.getAnnotation(BindView.class);
                if (bindView != null) {
                    int value = bindView.value();
                    ProxyInfo proxyInfo = mProxyMap.get(qualifedName);
                    if (proxyInfo == null) {
                        proxyInfo = new ProxyInfo();
                        mProxyMap.put(qualifedName, proxyInfo);
                    }
                    proxyInfo.value = value;
                    proxyInfo.packageName = packageName;
                    proxyInfo.typeElement = typeElement;
                    proxyInfo.setClassName(className);
                }
            } else if (element.getKind() == ElementKind.FIELD) {
                //filed type
                VariableElement variableElement = (VariableElement) element;
                //class type,通过variableElement获得上层封装拿到类的信息
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                //通过typeElement获取类的全名
                String qualifiedName = typeElement.getQualifiedName().toString();
                //得到类名
                String className = typeElement.getSimpleName().toString();
                //通过elementUtils获取包名
                String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                //获得此元素针对指定类型的注释（如果存在这样的注释），否则返回 null。
                BindView bindView = variableElement.getAnnotation(BindView.class);
                if (bindView != null) {
                    int value = bindView.value();
                    ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
                    //判断是否已经生成，没有则生成ProxyInfo对象
                    if (proxyInfo == null) {
                        proxyInfo = new ProxyInfo();
                        mProxyMap.put(qualifiedName, proxyInfo);
                    }
                    proxyInfo.mInjectElements.put(value, variableElement);
                    proxyInfo.typeElement = typeElement;
                    proxyInfo.packageName = packageName;
                    proxyInfo.value = value;
                    proxyInfo.setClassName(className);
                }
            } else {
                continue;
            }
        }
    }
}
