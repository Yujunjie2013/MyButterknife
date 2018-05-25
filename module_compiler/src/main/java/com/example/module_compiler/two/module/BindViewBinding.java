package com.example.module_compiler.two.module;

import com.example.module_annotation.BindView;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class BindViewBinding {
    //得到变量名
    public  String varName;
    //控件id
    public  int viewId;
    //变量类型
    public final TypeMirror typeMirror;

    public BindViewBinding(Element element) {
        VariableElement variableElement = (VariableElement) element;
        varName = variableElement.getSimpleName().toString();
        BindView annotation = element.getAnnotation(BindView.class);
        viewId = annotation.value();
        typeMirror = element.asType();
    }

}
