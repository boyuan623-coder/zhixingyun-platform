package com.jiawa.train.common.util;

import cn.hutool.core.util.IdUtil;

/** 雪花算法 ID 生成工具（common 模块）。 */
public class SnowUtil {

    private static long dataCenterId = 1;  //数据中心
    private static long workerId = 1;     //机器标识

    /** 生成下一个雪花 ID（long） */
    public static long getSnowflakeNextId() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextId();
    }

    /** 生成下一个雪花 ID（String，避免前端 Long 精度丢失） */
    public static String getSnowflakeNextIdStr() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextIdStr();
    }
}
