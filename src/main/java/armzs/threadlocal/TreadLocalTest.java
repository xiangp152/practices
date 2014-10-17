package test.armzs.threadlocal;

import java.util.Random;

/**
 * Created by xiangpeng on 2014/9/23.
 */
public class TreadLocalTest {

    public static void main(String[] args) {
        Thread t1 = new Thread(new Task());
        Thread t2 = new Thread(new Task());
        t1.start();
        t2.start();
    }

}


class Task implements Runnable {

    private ThreadLocal testBeanLocal = new ThreadLocal();

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is running");
        TestBean testBean = getBean();
        System.out.println("arg1 in " + Thread.currentThread().getName() + " is " + testBean.getArg1());
//        System.out.println("a is " + random.toString());
    }

    public TestBean getBean() {
        TestBean testBean = (TestBean) testBeanLocal.get();
        if (testBean == null) {
            testBean = new TestBean();
            Random random = new Random();
            testBean.setArg1(random.nextInt(100) + "");
            testBeanLocal.set(testBean);
        }
        return testBean;
    }
}

class TestBean {
    private String arg1;

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }
}