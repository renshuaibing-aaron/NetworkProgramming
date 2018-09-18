package com.aaron.ren.concurrency.threadpool;

import java.util.concurrent.*;

/**
 * 假设有多个线程执行若干任务，每个任务最多只能被执行一次。当多个线程试图
 * 同时执行同一个任务时，只允许一个线程执行任务，其他线程需要等待这个任务执行完后才
 * 能继续执行。下面是对应的示例代码。
 *
 * @author renshuaibing
 */
public class FutureTaskTest3
{

    private final ConcurrentMap <Object, Future <String>> taskCache = new ConcurrentHashMap <Object, Future <String>>();

    //当两个线程试图同时执行同一个任务时，如果Thread 1执行1.3后Thread 2执行2.1，那么接
    //下来Thread 2将在2.2等待，直到Thread 1执行完1.4后Thread 2才能从2.2（FutureTask.get()）返回。
    private String executionTask(final String taskName) throws ExecutionException, InterruptedException
    {
        while (true)
        {
            Future <String> future = taskCache.get(taskName); // 1.1,2.1
            if (future == null)
            {
                Callable <String> task = new Callable <String>()
                {
                    @Override
                    public String call() throws InterruptedException
                    {
                        return taskName;
                    }
                };
                FutureTask <String> futureTask = new FutureTask <String>(task);//1.2
                future = taskCache.putIfAbsent(taskName, futureTask); // 1.3
                if (future == null)
                {
                    future = futureTask;
                    futureTask.run(); // 1.4执行任务
                }
            }
            try
            {
                return future.get(); // 1.5,
            } catch (CancellationException e)
            {
                taskCache.remove(taskName, future);
            }
        }
    }
}
