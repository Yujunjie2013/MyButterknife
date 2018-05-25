package com.example.module_compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ProxyInfo {
    //类注解的值，布局id
    public int value;
    //生成的包名
    public String packageName;
    //类
    public TypeElement typeElement;
    /**
     * key为id，也就是成员变量注解的值，value为对应的成员变量
     */
    public Map<Integer, VariableElement> mInjectElements = new HashMap<>();
    //类名
    public String className;

    public Element getTypeElement() {
        return typeElement;
    }

//    /**
////     * 采用类名方式不能被混淆(否则编译阶段跟运行阶段，该字符串会不一样)，或者采用字符串方式
////     */
    public static final String PROXY = "ViewInject";
    public static final String ClassSuffix = "_" + PROXY;

    public String getProxyclassFullName() {
        return typeElement.getQualifiedName().toString() + ClassSuffix;
    }

    public String getClassName() {
        return typeElement.getSimpleName().toString() + ClassSuffix;
    }

    public void setClassName(String className) {
        this.className = className + ClassSuffix;
    }

    public String genrateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("package ")
                .append(packageName)
                .append(";\n\n")
                .append("import com.example.module_compiler.*;\n")
                .append("import com.example.module_annotation.*;\n")
                .append("import com.example.module_api.*;\n")
                .append("import android.support.annotation.Keep;\n")
                .append("import android.view.View;\n\n")
                .append("import ")
                .append(typeElement.getQualifiedName())
                .append(";\n\n")
                .append("@Keep")
                .append("\n")
                .append("public class ").append(getClassName())
                .append(" {\n");
//                .append(" implements ").append(PROXY).append("<").append(typeElement.getQualifiedName()).append(">").append(" {\n");
        generateMethod(builder);
        builder.append("\n}\n");
        return builder.toString();
    }

    private void generateMethod(StringBuilder builder) {
//        builder.append("over\n")
//        builder.append("public void inject(")
//                .append(typeElement.getQualifiedName())
//                .append(" host,Object object){\n");
        builder.append("public ")
                .append(getClassName())
                .append("(final ")
                .append(typeElement.getSimpleName())
                .append(" host,View object){\n");
        for (int id : mInjectElements.keySet()) {
            VariableElement variableElement = mInjectElements.get(id);
            String name = variableElement.getSimpleName().toString();
            String type = variableElement.asType().toString();
            //这里如果object不为null，则可以传入view对象
            builder.append("if (object instanceof android.view.View) {\n")
                    .append("host.").append(name).append("=").append("(").append(type).append(")").append("((android.view.View)object).findViewById(")
                    .append(id).append(");\n}")
                    .append("else {\n")
                    .append("host.").append(name).append("=").append("(").append(type).append(")host.findViewById(").append(id).append(");\n}");
        }
        builder.append("\n}");

    }


}
