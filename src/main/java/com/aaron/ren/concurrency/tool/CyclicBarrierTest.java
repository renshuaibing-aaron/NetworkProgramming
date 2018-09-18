package com.aaron.ren.concurrency.tool;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.*;

/**
 * 关卡：CyclicBarrier
 *
 * @author renshuaibing
 */
public class CyclicBarrierTest
{
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue <Runnable>());



    /*当拦截线程数达到4时，便优先执行barrierAction，然后再执行被拦截的线程。
    CyclicBarrier默认的构造方法是CyclicBarrier（int parties），其参数表示屏障拦截的线程数
    量，每个线程调用await方法告诉CyclicBarrier我已经到达了屏障，然后当前线程被阻塞。
     看了一下CyclicBarrier源码 应该是最后一个进入await的线程会执行barrierAction的run方法，而且不会被阻塞，
     由这个线程调用signalAll方法唤醒其他等待的线程，自己则直接返回。
     所以最后一个线程总是先往下执行。因此就看哪个线程最后调用await方法了，问题也就变成了新建的线程和主线程谁先调用await方法了。
    */
    private static final CyclicBarrier cb = new CyclicBarrier(4, new Runnable()
    {
        @Override
        public void run()
        {
            System.out.println("寝室四兄弟一起出发去球场");
        }
    });

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
            try
            {
                Thread.sleep(1000);
                cb.await();//拦截线程
                System.out.println(name + "从楼底下出发");
                Thread.sleep(1000);
                System.out.println(name + "到达操场");

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } catch (BrokenBarrierException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        String[] str = {"李明", "王强", "刘凯", "赵杰"};
        for (int i = 0; i < 4; i++)
        {
            threadPool.execute(new GoThread(str[i]));
        }
        try
        {
            Thread.sleep(4000);
            System.out.println("四个人一起到达球场，现在开始打球");
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

}
