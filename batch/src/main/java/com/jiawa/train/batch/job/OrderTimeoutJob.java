package com.jiawa.train.batch.job;

import com.jiawa.train.batch.feign.BusinessFeign;
import com.jiawa.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Quartz 订单超时自动取消（INIT/PENDING 超时回滚库存）。
 * 通过 JobController 动态注册，建议每分钟执行一次。
 */
@DisallowConcurrentExecution
public class OrderTimeoutJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(OrderTimeoutJob.class);

    @Resource
    private BusinessFeign businessFeign;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("OrderTimeoutJob 开始");
        CommonResp<Map<String, Object>> resp = businessFeign.cancelTimeoutOrders(null);
        LOG.info("OrderTimeoutJob 结束：{}", resp == null ? null : resp.getContent());
    }
}
