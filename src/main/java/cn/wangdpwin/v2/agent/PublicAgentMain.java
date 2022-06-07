package cn.wangdpwin.v2.agent;

import javassist.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @Author wangdongpeng
 * @Date 2022/6/7 2:52 下午
 * @Version 1.0
 * 监控所有的方法类
 */
public class PublicAgentMain {

    //javaagent 入口方法
    // 以 arg 为前缀的类才会进行插桩处理 -javaagent:xxx.jar=com.sys.insertPile
    public static void premain(String arg, Instrumentation instrumentation) {
        System.out.println("hello agent!!!!!");
        System.out.println("arg: " + arg);

        final String config = arg;

        // 使用 javassist ,在运行时修改 class 字节码，就是 插桩
        final ClassPool pool = new ClassPool();
        pool.appendSystemPath();

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
//                System.out.println(className);
                if (className == null || !className.replaceAll("/",".").startsWith(config)) {
                    return null;
                }

                try {
                    className = className.replaceAll("/", ".");
                    CtClass ctClass = pool.get(className);
                    // 获得类中的所有方法
                    for (CtMethod declaredMethod : ctClass.getDeclaredMethods()) {
                        newMethod(declaredMethod);
                    }
                    return ctClass.toBytecode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
      对方法进行增强，类似于springAOP的$proxy代理类
      eg: 原方法为 sayHi(System.out.println("source"));
          将该方法复制一份，起名为sayHi$agent("System.out.println("source agent")");
          将原方法改造为
            public void sayHi() {
                 long begin = System.currentTimeMillis();
                 sayHi$agent();
                 long end = System.currentTimeMillis();
                 System.out.println(':' + end - begin);
            }
          sayHi$agent方法复制了原方法的代码，执行具体业务，sayHi内容调用 sayHi$agent 方法，在前后增加了计时功能，
          因为前置插入的变量 begin 在后置中无妨访问，为避免异常才使用复制新方法并引用的方式，try catch避免出现异常导致中断，
          ($w)sayHi$agent($$)  $w强制类型转换，$$方法参数
     */
    private static CtMethod newMethod(CtMethod oldMethod) {
        CtMethod copy = null;
        try {
            //1. 将方法进行复制
            copy = CtNewMethod.copy(oldMethod, oldMethod.getDeclaringClass(), null);
            //类似于使用动态代理
            copy.setName(oldMethod.getName() + "$agent");
            //类文件中添加 sayHello$agent 方法
            oldMethod.getDeclaringClass().addMethod(copy);

            //2. 改变原有的方法,将 原有的 sayHello 方法进行重写操作
            if (oldMethod.getReturnType().equals(CtClass.voidType)) {
                oldMethod.setBody(String.format(voidSource, oldMethod.getName()));
            } else {
                oldMethod.setBody(String.format(source, oldMethod.getName()));
            }
        } catch (CannotCompileException | NotFoundException e) {
            e.printStackTrace();
        }
        return copy;

    }

    /**
     * 参数的封装
     * $$ ======》 arg1, arg2, arg3
     * $1 ======》 arg1
     * $2 ======》 arg2
     * $3 ======》 arg3
     * $args ======》 Object[]
     */
    //有返回值得方法
    final static String source = "{ long begin = System.currentTimeMillis();\n" +
            "        Object result;\n" +
            "        try {\n" +
            "            result = ($w)%s$agent($$);\n" +
            "        } finally {\n" +
            "            long end = System.currentTimeMillis();\n" +
            "            System.out.println(\"耗时：\" + (end - begin));\n" +
            "        }\n" +
            "        return ($r) result;}";

    //没有返回值的方法
    final static String voidSource = "{long begin = System.currentTimeMillis();\n" +
            "        try {\n" +
            "            %s$agent($$);\n" +
            "        } finally {\n" +
            "            long end = System.currentTimeMillis();\n" +
            "            System.out.println(\"耗时：\" + (end - begin));\n" +
            "        }}";


}