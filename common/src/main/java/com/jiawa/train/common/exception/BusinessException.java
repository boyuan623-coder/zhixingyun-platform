package com.jiawa.train.common.exception;

/** 业务异常（common 模块）。 */
public class BusinessException extends RuntimeException {

    private BusinessExceptionEnum e;

    public BusinessException(BusinessExceptionEnum e) {
        this.e = e;
    }

    public BusinessExceptionEnum getE() {
        return e;
    }

    public void setE(BusinessExceptionEnum e) {
        this.e = e;
    }

    /**
     * 不填充堆栈信息，提高高频业务异常的抛出性能。
     * 业务异常不需要堆栈定位，message 已由 {@link BusinessExceptionEnum#getDesc()} 提供。
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
