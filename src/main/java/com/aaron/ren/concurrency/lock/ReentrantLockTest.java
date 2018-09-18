package com.aaron.ren.concurrency.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RetrantLockTest 模拟实现一个有界阻塞队列
 * (The {@link java.util.concurrent.ArrayBlockingQueue} class provides
 * this functionality, so there is no reason to implement this
 * *sample usage class.)
 *
 * @author renshuaibing
 */
public class ReentrantLockTest
{
    final ReentrantLock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    final Object[] items = new Object[10];
    int putptr, takeptr, count;

    public void put(Object x) throws InterruptedException
    {
        lock.lock();
        try
        {
            // 当数组满了
            while (count == items.length)
            {
                System.out.println("=========当数组满了,等待===========");
                // 释放锁，等待
                notFull.await();
            }
            // 放入数据
            items[putptr] = x;
            // 如果到最后一个位置了,下标从头开始,防止下标越界
            if (++putptr == items.length)
            {
                // 从头开始
                putptr = 0;
            }
            // 对 count ++ 加加
            ++count;
            // 通知 take 线程,可以取数据了,不必继续阻塞
            notEmpty.signal();
        } finally
        {
            lock.unlock();
        }
    }

    public Object take() throws InterruptedException
    {
        lock.lock();
        try
        {
            // 如果数组没有数据,则等待
            while (count == 0)
            {
                notEmpty.await();
            }
            // 取数据
            Object x = items[takeptr];
            // 如果到数组尽头了,就从头开始
            if (++takeptr == items.length)
            {
                // 从头开始
                takeptr = 0;
            }
            // 将数量减1
            --count;
            // 通知阻塞的 put 线程可以装填数据了
            notFull.signal();
            return x;
        } finally
        {
            lock.unlock();
        }
    }

    public static void main(String[] args)
    {

        final ReentrantLockTest blockQueue = new ReentrantLockTest();

        Thread thread1 = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 1; i < 100; i++)
                {
                    try
                    {
                        System.out.println("==========put===========" + i);
                        blockQueue.put(i);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread thread2 = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 1; i < 100; i++)
                {
                    try
                    {
                        Object obj = blockQueue.take();
                        System.out.println("*********take************" + obj);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread1.start();
        thread2.start();
    }
}