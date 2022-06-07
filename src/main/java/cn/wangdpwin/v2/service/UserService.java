package cn.wangdpwin.v2.service;

/**
 * @Author wangdongpeng
 * @Date 2022/6/7 3:04 下午
 * @Version 1.0
 */
public class UserService {

    public void sayHello(String s) throws InterruptedException {
        Thread.sleep(50);
        System.out.println("hello world!!! " + s);
    }


    public Integer sayHelloReturn(String s, Integer age) throws InterruptedException {
        Thread.sleep(100);
        System.out.println("hello world!!! " + s + "age = " + age);
        return age;
    }

    public String sayHelloReturnEvery(String name, Integer age, String phone) throws InterruptedException {
        Thread.sleep(200);
        return (name + " 的年龄是 " + age + " 电话是 " + phone);
    }


}
