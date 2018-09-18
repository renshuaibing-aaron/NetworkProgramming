package com.aaron.ren.concurrency.tool;

import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通过CountDownLatch实现CyclicBarrier
 * CountDownLatch通过将await()方法和countDown()方法在不同线程组分别调用，从而实现线程组间的线程等待，即一个线程组等待另一个线程组执行结束再执行。
 * 而CyclicBarrier类则是通过调用await()方法实现线程组内的线程等待，即达到需要拦截的线程数，被拦截的线程才会依次获取锁，释放锁。
 * 那么将CountDownLatch的用法进行转换，即在同一个线程组内调用await()方法和countDown()方法，则可实现CyclicBarrier类的功能。
 * 但是注意的是必须先调用countDown(）方法，才能调用await()方法，因为一旦调用await()方法，该线程后面的内容便不再执行，那么count值无法改变。
 * @author renshuaibing
 */
public class CyclicBarrierWithCount
{
    private final static CountDownLatch cdl = new CountDownLatch(3);
    private final static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 15, 60, TimeUnit.SECONDS, new LinkedBlockingQueue <Runnable>());//使用线程池

    private static class GoThread extends Thread
    {
        private final String name;

        public GoThread(String name)
        {
            this.name = name;

        }

        @Override
        public void run()
        {
            System.out.println(name + "开始从宿舍出发");
            cdl.countDown();
            try
            {
                Thread.sleep(1000);
                cdl.await();//拦截线程
                System.out.println(name + "从楼底下出发");
                Thread.sleep(1000);
                System.out.println(name + "到达操场");
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

        String[] str = {"李明", "王强", "刘凯", "赵杰"};
        String[] str1 = {"王二", "洪光", "雷兵", "赵三"};
        for (int i = 0; i < 4; i++)
        {
            threadPool.execute(new GoThread(str[i]));
        }
        try
        {
            Thread.sleep(4000);
            System.out.println("四个人一起到达球场，现在开始打球");
            System.out.println("现在对CyclicBarrier进行复用.....");
            System.out.println("又来了一拨人，看看愿不愿意一起打：");
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < 4; i++)
        {
            threadPool.execute(new GoThread(str1[i]));
        }
        try
        {
            Thread.sleep(4000);
            System.out.println("四个人一起到达球场，表示愿意一起打球，现在八个人开始打球");
            //System.out.println("现在对CyclicBarrier进行复用");
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
