package com.sys.insertPilepublic;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @Author wangdongpeng
 * @Date 2022/6/7 4:24 下午
 * @Version 1.0
 */

public class TimeCountAgent implements ClassFileTransformer {

    /**
     * 方法返回一个字节数组,其实就是修改后的字节码.
     * 然后就会替换掉classloader中的类描述信息
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String newClassName = className.replace("/", ".");
        ClassPool pool = ClassPool.getDefault();
        try{
            CtClass ctClass = pool.get(className);;
            /** 类信息是否处于冻结状态 */
            if(ctClass.isFrozen()){
                return null;
            }
            CtMethod[] methods = ctClass.getDeclaredMethods();
            byte[] result = null;
            if(methods != null && methods.length > 0){
                for(CtMethod method : methods){
                    String name = method.getName();
                    /** 2种实现 */
                    //addTimeCountMethod2(method);
                    addTimeCountMethod(ctClass,name,method);
                }
                return ctClass.toBytecode();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 新增方法 的实现
     */
    private byte[] addTimeCountMethod(CtClass cc,String oldName,CtMethod method) throws Exception{
        if(cc.isFrozen()){
            return null;/** 不需要转换 */
        }
        String newName = oldName + "tmp";
        /** 重命名老方法 */
        method.setName(newName);
        /** 新建一个 方法 名字 跟老方法一样 */
        CtMethod newMethod = CtNewMethod.copy(method, oldName, cc, null);
        String type = method.getReturnType().getName();
        StringBuilder body = new StringBuilder();
        /** 新方法增加计时器 */
        body.append("{\n long start = System.currentTimeMillis();\n");
        /** 判断老方法返回类型 */
        if(!"void".equals(type)){
            /** 类似 Object result = newName(args0,args1); */
            body.append(type + " result = ");
        }
        /** 执行老方法 */
        body.append( newName + "($$);\n");
        /** 统计执行完成时间 */
        body.append("System.out.println(\"Call to method " + oldName +
                " took \" +\n (System.currentTimeMillis()-start) + " +
                "\" ms.\");\n");
        if(!"void".equals(type)){
            /** 类似 Object result = newName(args0,args1); */
            body.append("return result;\n");
        }
        body.append("}");
        newMethod.setBody(body.toString());
        cc.addMethod(newMethod);
        return cc.toBytecode();
    }

    /**
     * 修改方法体
     */
    private void addTimeCountMethod2(CtMethod method) throws Exception {
        method.addLocalVariable("start", CtClass.longType);
        method.insertBefore("start = System.currentTimeMillis();");
        method.insertAfter("System.out.println(\"exec time is :\" + (System.currentTimeMillis() - start) + \"ms\");");
    }
}