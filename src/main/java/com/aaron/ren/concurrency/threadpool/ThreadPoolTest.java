package com.aaron.ren.concurrency.threadpool;

import java.util.concurrent.*;

/**
 * @author renshuaibing
 */
public class ThreadPoolTest
{
    public static void main(String[] args)
    {

        int corePoolSize = 10;
        int maximumPoolSize = 10;
        long keepAliveTime = 1000;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue <Runnable> workQueue = new ArrayBlockingQueue <Runnable>(10);

        //线程工厂怎么实现
        // ThreadFactory threadFactory=new ThreadFactoryBuilder().setNameFormat("XX-task-%d").build() ;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                //线程池的基本大小
                corePoolSize,
                //线程池最大数量
                maximumPoolSize,
                //线程活动保持时间
                keepAliveTime,
                //线程活动保持时间的单位
                unit,
                //任务队列
                workQueue,
                //创建线程的工厂
                new ThreadFactory()
                {
                    @Override
                    public Thread newThread(Runnable r)
                    {
                        Thread t = new Thread(r);
                        t.setName("生成线程");
                        System.out.println("生成线程 " + t.getName());

                        return t;
                    }
                },
                //饱和策略
                handler);
        threadPoolExecutor.submit(new Runnable()
        {
            @Override
            public void run()
            {

            }
        });
        threadPoolExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {

            }
        });
    }
}
