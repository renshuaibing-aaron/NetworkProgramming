package com.aaron.ren.concurrency.tool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 闭锁
 * CountDownLatch主要用于多线程环境中，当所有的线程都countDown了，就会释放所有的等待的线程，await在到0之前一直等待。
 * 由于countDown方法可以用在任何地方，所以这里说的N个
 * 点，可以是N个线程，也可以是1个线程里的N个执行步骤。用在多个线程时，只需要把这个
 * CountDownLatch的引用传递到线程里即可。
 * 一个线程调用countDown方法happen-before，另外一个线程调用await方法。
 * @author renshuaibing
 */
public class CountDownLatchTest
{

    public static void main(String[] args)
    {
        ThreadPoolExecutor poolExe = new ThreadPoolExecutor(100, 1000, 1, TimeUnit.SECONDS, new LinkedBlockingDeque <Runnable>(100));
        // 考试开始铃声响起，考试开始
        final CountDownLatch examBegin = new CountDownLatch(1);
        // 单个考生，考试结束交卷
        final CountDownLatch student = new CountDownLatch(10);

        // 一个考场10位考生
        for (int i = 0; i < 10; i++)
        {
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        System.out.println("考生" + Thread.currentThread().getName() + "在等待考试开始的铃声响起");
                        examBegin.await();
                        System.out.println("考生听到铃声" + Thread.currentThread().getName() + "开始答题");
                        Thread.sleep((long) (Math.random() * 100));//答题过程，真正的业务逻辑处理部分
                        System.out.println("考生" + Thread.currentThread().getName() + "交卷");
                        student.countDown();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            poolExe.execute(runnable); // 运动员开始任务
        }

        /*examBegin只有在countDown后，10个考生才有可能开始答题，因为答题前有examBegin.await()，会阻塞住；
        同理，在考试结束前，student.await()会阻塞住，因为只有当所有的学生都交卷了（countDown），await才会通过。*/
        try
        {
            // 答题时间
            Thread.sleep((long) (Math.random() * 10000));
            System.out.println("考场" + Thread.currentThread().getName() + "开始铃声即将响起");
            examBegin.countDown(); // 命令计数器置为0
            System.out.println("考场" + Thread.currentThread().getName() + "考试开始铃声响起");
            student.await(); // 所有考生交卷
            System.out.println("考场" + Thread.currentThread().getName() + "考试结束");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        poolExe.shutdown();

    }

}
