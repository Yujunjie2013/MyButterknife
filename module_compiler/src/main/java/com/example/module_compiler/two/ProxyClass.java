package com.example.module_compiler.two;

import com.example.module_compiler.two.module.BindViewBinding;
import com.example.module_compiler.two.module.ClassModuleBinding;
import com.example.module_compiler.two.module.OnClickBinding;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ProxyClass {

    private TypeElement typeElement;
    private String packageName;
    private List<ClassModuleBinding> classModuleBindingList = new ArrayList<>();
    private List<BindViewBinding> bindViewBindings = new ArrayList<>();
    private List<OnClickBinding> clickBindingList = new ArrayList<>();

    public ProxyClass(TypeElement typeElement, String packageName) {
        this.typeElement = typeElement;
        this.packageName = packageName;
    }

    public void add(ClassModuleBinding classModuleBinding) {
        classModuleBindingList.add(classModuleBinding);
    }

    public void add(BindViewBinding viewBinding) {
        bindViewBindings.add(viewBinding);
    }

    public void add(OnClickBinding clickBinding) {
        clickBindingList.add(clickBinding);
    }

    //proxytool.IProxy
    public static final ClassName IPROXY = ClassName.get("com.example.module_api", "ViewInject");
    //android.view.View
    public static final ClassName VIEW = ClassName.get("android.view", "View");
    //android.view.View.OnClickListener
    public static final ClassName VIEW_ON_CLICK_LISTENER = ClassName.get("android.view", "View", "OnClickListener");
    //生成代理类的后缀名
    public static final String SUFFIX = "$$Proxy";

    //生成代理类
    public JavaFile generateProxy() {
        //生成public void inject(final T target, View root)方法
        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject")
//                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(typeElement.asType()), "target", Modifier.FINAL)
                .addParameter(VIEW, "root");

        for (ClassModuleBinding classModuleBinding : classModuleBindingList) {
            inject.addStatement("target.setContentView($L)",classModuleBinding.layoutId);
        }

        //在inject方法中，添加我们的findViewById逻辑
        for (BindViewBinding viewBinding : bindViewBindings) {
            inject.addStatement("target.$N=($T)root.findViewById($L)", viewBinding.varName, ClassName.get(viewBinding.typeMirror), viewBinding.viewId);
        }
        if (clickBindingList.size() > 0) {
            inject.addStatement("$T listener", VIEW_ON_CLICK_LISTENER);
        }

//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        for (OnClickBinding clickBinding : clickBindingList) {
            //匿名内部类
            TypeSpec build = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(VIEW_ON_CLICK_LISTENER)
                    .addMethod(MethodSpec.methodBuilder("onClick")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(TypeName.VOID)
                            .addParameter(VIEW, "view")
                            .addStatement("target.$N($L)", clickBinding.methodName, clickBinding.mParameterEixt ? clickBinding.parametersName : "")
                            .build()
                    ).build();
            inject.addStatement("listener=$L", build);
            for (int id : clickBinding.ids) {
                //设置点击监听
                inject.addStatement("root.findViewById($L).setOnClickListener(listener)", id);
            }
        }

        //添加以$$Proxy为后缀的类
        TypeSpec classType = TypeSpec.classBuilder(typeElement.getSimpleName() + SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(inject.build())
                .addSuperinterface(ParameterizedTypeName.get(IPROXY, TypeName.get(typeElement.asType())))
                .build();

        //添加包名
        JavaFile javaFile = JavaFile.builder(packageName, classType).build();
        return javaFile;

    }
}
