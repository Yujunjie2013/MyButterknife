package com.example.module_compiler.two.module;

import com.example.module_annotation.BindLayout;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class ClassModuleBinding {

    public int layoutId;

    public ClassModuleBinding(Element element) {
//        TypeElement typeElement = (TypeElement) element;
        BindLayout annotation = element.getAnnotation(BindLayout.class);
        layoutId = annotation.value();
    }
}
