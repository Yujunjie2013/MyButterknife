package com.example.module_compiler.two;

import com.example.module_annotation.BindLayout;
import com.example.module_annotation.BindView;
import com.example.module_annotation.OnClick;
import com.example.module_compiler.two.module.BindViewBinding;
import com.example.module_compiler.two.module.ClassModuleBinding;
import com.example.module_compiler.two.module.OnClickBinding;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
//这句注解必须要加，否则没效果
@AutoService(Processor.class)
public class ProxyProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    //存放收集到的信息,key为全限定名
    private HashMap<String, ProxyClass> proxyClassMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //跟文件相关的辅助类，生成JavaSourceCode.
        filer = processingEnvironment.getFiler();
        //跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
        messager = processingEnvironment.getMessager();
        //跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
        elementUtils = processingEnvironment.getElementUtils();
    }

    //返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //返回支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(BindLayout.class.getCanonicalName());
        hashSet.add(BindView.class.getCanonicalName());
        hashSet.add(OnClick.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        proxyClassMap.clear();
        collection(roundEnvironment);
        generateJavaCode();
        return true;
    }

    private void generateJavaCode() {
        for (String key : proxyClassMap.keySet()) {
            ProxyClass proxyClass = proxyClassMap.get(key);
            try {
                JavaFile javaFile = proxyClass.generateProxy();
                javaFile.writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean collection(RoundEnvironment roundEnvironment) {
        //获取指定注释类型注解的元素
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindLayout.class);
        for (Element element : elementsAnnotatedWith) {
            if (!checkElement(BindLayout.class, "class", element)) {
                return true;
            }
            parseLayoutId(element);
        }

        Set<? extends Element> bindViewSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : bindViewSet) {
            if (!checkElement(BindView.class, "filed", element)) {
                return true;
            }
            parseBindView(element);
        }
        Set<? extends Element> onClickSet = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        for (Element element : onClickSet) {
            if (!checkElement(OnClick.class, "method", element)) {
                return true;
            }
            parseOnClick(element);
        }
        return true;

    }

    private void parseOnClick(Element element) {
        ProxyClass proxyClass = getProxyClass(element);
        if (proxyClass != null) {
            OnClickBinding clickBinding = new OnClickBinding(element);
            proxyClass.add(clickBinding);
        }
    }

    private void parseBindView(Element element) {
        ProxyClass proxyClass = getProxyClass(element);
        if (proxyClass != null) {
            BindViewBinding viewBinding = new BindViewBinding(element);
            proxyClass.add(viewBinding);
        }
    }

    private void parseLayoutId(Element element) {
        ProxyClass proxyClass = getProxyClass(element);
        if (proxyClass != null) {
            ClassModuleBinding classModuleBinding = new ClassModuleBinding(element);
            proxyClass.add(classModuleBinding);
        }
    }

    private ProxyClass getProxyClass(Element element) {
        Element element1 = null;
        if (element.getKind() == ElementKind.CLASS) {
            element1 = element;
        } else {
            element1 = element.getEnclosingElement();
        }
        if (element1.getKind() == ElementKind.CLASS) {
            TypeElement enclosingElement = (TypeElement) element1;
            String qualifiedName = enclosingElement.getQualifiedName().toString();
            ProxyClass proxyClass = proxyClassMap.get(qualifiedName);
            //获取到包名
            String packageName = elementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
            if (proxyClass == null) {
                proxyClass = new ProxyClass(enclosingElement, packageName);
                proxyClassMap.put(qualifiedName, proxyClass);
            }
            return proxyClass;
        }
        return null;
    }

    //检查元素是否合法
    private boolean checkElement(Class<? extends Annotation> annotationClass, String targetThing, Element element) {
        boolean check = true;
        //得到父类元素
        Element superElement = null;
        if (element.getKind() == ElementKind.CLASS) {
            superElement = element;
        } else {
            superElement = element.getEnclosingElement();
        }
        if (superElement.getKind() == ElementKind.CLASS) {
            TypeElement enclosingElement = (TypeElement) superElement;
            //父元素的全限定名
            String qualifedName = enclosingElement.getQualifiedName().toString();
            //得到元素的修饰符
            Set<Modifier> modifiers = element.getModifiers();
            // 所在的类不能是private或static修饰
            if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
                error(element, "@%s %s 所在类不能是private或者static修饰(%s %s)", annotationClass.getSimpleName(),
                        targetThing, qualifedName, element.getSimpleName());
                check = false;
            }
            //父元素必须是类，而不能是接口或者枚举
            if (enclosingElement.getKind() != ElementKind.CLASS) {
                error(element, "@%s %s 父元素必须是类(%s %s)", annotationClass.getSimpleName(), targetThing,
                        qualifedName, element.getSimpleName());
                check = false;
            }
            //不能再android和java框架层注解
            if (qualifedName.startsWith("android.") || qualifedName.startsWith("java.")) {
                error(element, "@%s 注解不能用于android或者java框架层(%s)", annotationClass.getSimpleName(), qualifedName);
                check = false;
            }
        } else {
            error(element, "@%s %s 父元素必须是类(%s %s)", annotationClass.getSimpleName(), targetThing,
                    superElement.getEnclosingElement().getSimpleName(), element.getSimpleName());
            check = false;
        }
        return check;
    }


    private void error(Element e, String msg, Object... args) {
        //参数1、表示打印日志错误级别,参数2要打印的内容,参数3是哪个元素
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

}
