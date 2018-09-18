package com.aaron.ren.concurrency.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在很多高并发的环境下，往往我们只需要某些任务只执行一次。这种使用情景FutureTask的特性恰能胜任。
 * 举一个例子，假设有一个带key的连接池，当key存在时，即直接返回key对应的对象；当key不存在时，则创建连接。
 * 对于这样的应用场景，通常采用的方法为使用一个Map对象来存储key和连接池对应的对应关系，典型的代码如下面所示：
 * 在上面的例子中，我们通过加锁确保高并发环境下的线程安全，也确保了connection只创建一次，然而确牺牲了性能。
 * 改用ConcurrentHash的情况下，几乎可以避免加锁的操作，性能大大提高，但是在高并发的情况下有可能出现Connection被创建多次的现象。
 * 这时最需要解决的问题就是当key不存在时，创建Connection的动作能放在connectionPool之后执行，这正是FutureTask发挥作用的时机，基于ConcurrentHashMap和FutureTask的改造代码如下：
 * @author renshuaibing
 */
public class FutureTaskTest2
{

    public static AtomicInteger count = new AtomicInteger(0);

    private static Map <String, Integer> connectionmapPool = new HashMap <String, Integer>();
    private static  ConcurrentHashMap <String, FutureTask <Integer>> connectionPool = new ConcurrentHashMap <String, FutureTask <Integer>>();

    public static int getConnectionByFutureTask(final String key) throws Exception
    {
        FutureTask <Integer> connectionTask = connectionPool.get(key);
        if (connectionTask != null)
        {
            return connectionTask.get();
        } else
        {
            Callable <Integer> callable = new Callable <Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    // TODO Auto-generated method stub
                    return createConnection(key);
                }
            };
            FutureTask <Integer> newTask = new FutureTask <Integer>(callable);
            connectionTask = connectionPool.putIfAbsent(key, newTask);
            if (connectionTask == null)
            {
                connectionTask = newTask;
                connectionTask.run();
            }
            //阻塞获取
            return connectionTask.get();
        }
    }

    public static Integer getConnectionByConcurrentHashMap(String key)
    {
        if (connectionmapPool.containsKey(key))
        {
            System.out.println("*******获取Connection*********"+connectionmapPool.get(key));
            return connectionmapPool.get(key);
        } else
        {
            //创建 Connection
            int conn = createConnection(key);
            connectionmapPool.put(key, conn);
            return conn;
        }
    }

    //创建Connection
    private static Integer createConnection(String key)
    {
       Integer result=Integer.valueOf(count.get());
        System.out.println("*******创建Connection*key*"+key+"***result****"+result);
        return result;
    }


    private static class CallableTask implements  Callable<Integer>{

        private int key;

        public CallableTask(int i)
        {
            this.key=i;
        }

        @Override
        public Integer call() throws Exception
        {
            return FutureTaskTest2.getConnectionByConcurrentHashMap(String.valueOf(key));
        }
    }
    private static class CallableTask2 implements  Callable<Integer>{

        private int key;

        public CallableTask2(int i)
        {
            this.key=i;
        }

        @Override
        public Integer call() throws Exception
        {
            return FutureTaskTest2.getConnectionByFutureTask(String.valueOf(key));
        }
    }

    public static void main(String[] args)
    {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for( int i=0;i<10;i++){
            executor.submit(new CallableTask(i));
        }
        System.out.println("----------------------------");
        for( int i=0;i<10;i++){
            executor.submit(new CallableTask2(i));
        }
    }

}
