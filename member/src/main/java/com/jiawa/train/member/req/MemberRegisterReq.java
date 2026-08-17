package com.jiawa.train.member.req;

import jakarta.validation.constraints.NotBlank;

/** 会员显式注册请求 DTO。 */
public class MemberRegisterReq {

    /** 待注册手机号 */
    @NotBlank(message = "【手机号】不能为空")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "MemberRegisterReq{" +
                "mobile='" + mobile + '\'' +
                '}';
    }
}
