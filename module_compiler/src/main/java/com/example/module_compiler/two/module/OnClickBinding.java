package com.example.module_compiler.two.module;

import com.example.module_annotation.OnClick;
import com.example.module_compiler.two.ProxyClass;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class OnClickBinding {
    public int[] ids;
    public String methodName;
    public boolean mParameterEixt;
    public String parametersName;

    public OnClickBinding(Element element) {
        ExecutableElement executableElement = (ExecutableElement) element;
        //方法名
        methodName = executableElement.getSimpleName().toString();

        //得到参数集合
        List<? extends VariableElement> parameters = executableElement.getParameters();
        if (parameters.size() > 1) {
            throw new IllegalArgumentException(String.format("@%s 该方法参数必须为一个", methodName));
        }

        if (parameters.size() == 1) {
            //刚好是一个参数
            VariableElement variableElement = parameters.get(0);
            //判断参数是否是View类型,如果不是抛异常
            if (!variableElement.asType().toString().equals(ProxyClass.VIEW.toString())) {
                throw new IllegalArgumentException(String.format("@%s 该方法参数必须是%s", methodName, ProxyClass.VIEW.toString()));
            }
            mParameterEixt=true;
            parametersName = variableElement.getSimpleName().toString();

        }
        OnClick annotation = element.getAnnotation(OnClick.class);
        //得到所有的控件id
        ids = annotation.value();

//        TypeMirror typeMirror = executableElement.asType();
    }
}
