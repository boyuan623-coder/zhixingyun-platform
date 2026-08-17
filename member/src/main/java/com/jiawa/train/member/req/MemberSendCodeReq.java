package com.jiawa.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 发送短信验证码请求 DTO。 */
public class MemberSendCodeReq {

    /** 接收验证码的手机号 */
    @NotBlank(message = "【手机号】不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "MemberSendCodeReq{" +
                "mobile='" + mobile + '\'' +
                '}';
    }
}
