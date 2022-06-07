package cn.wangdpwin.v1;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author wangdongpeng
 * @Date 2022/6/7 2:21 下午
 * @Version 1.0
 */
public class SqlTraceClassFileTransformer implements ClassFileTransformer {

    private static final Set<String> CLASS_NAME_SET = new HashSet<>();

    {
        CLASS_NAME_SET.add("org.apache.ibatis.executor.statement.PreparedStatementHandler");
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!CLASS_NAME_SET.contains(className)) {
            return null;
        }
        CtClass ctclass = null;
        try {
            ctclass = ClassPool.getDefault().get(className);// 使用全称,用于取得字节码类<使用javassist>

            CtMethod queryMethod = ctclass.getDeclaredMethod("query");
            queryMethod.insertBefore("long l = System.currentTimeMillis();");
//            queryMethod.insertAfter("System.out.println("执行完毕用时:" + (System.currentTimeMillis() - l));");
            queryMethod.insertAfter("System.out.println(l)");
            queryMethod.insertAfter("System.out.println(System.currentTimeMillis()-l)");

            return ctclass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}

