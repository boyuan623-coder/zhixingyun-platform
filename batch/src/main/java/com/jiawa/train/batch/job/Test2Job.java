package com.jiawa.train.batch.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz 测试任务 2，用于多任务调度场景验证。
 */

@DisallowConcurrentExecution
public class Test2Job implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("TestJob222222 TEST开始");
        // try {
        //     Thread.sleep(3000);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        System.out.println("TestJob222222 TEST结束");
    }
}
