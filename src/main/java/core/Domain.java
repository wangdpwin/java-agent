package core;

/**
 * @Author wangdongpeng
 * @Date 2022/6/7 2:19 下午
 * @Version 1.0
 */
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import java.lang.instrument.Instrumentation;

public class Domain {

    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("premain ===> " + args);
        instrumentation.addTransformer(new SqlTraceClassFileTransformer());
    }


    public static void main(String[] args) throws Exception {
        CtClass ctClass = ClassPool.getDefault().get("core.DoMain");
        CtMethod premain = ctClass.getDeclaredMethod("premain", new CtClass[]{
                ClassPool.getDefault().get("java.lang.String"),
                ClassPool.getDefault().get("java.lang.instrument.Instrumentation")
        });

        System.out.println(premain.getLongName());
    }

}

