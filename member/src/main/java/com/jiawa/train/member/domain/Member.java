package com.jiawa.train.member.domain;

/** 会员领域实体，对应数据库表 member。 */
public class Member {
    /** 会员主键（雪花 ID） */
    private Long id;

    /** 手机号，登录与发码的唯一业务标识 */
    private String mobile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", mobile=").append(mobile);
        sb.append("]");
        return sb.toString();
    }
}