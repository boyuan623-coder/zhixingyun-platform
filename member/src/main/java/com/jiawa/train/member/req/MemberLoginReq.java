package com.jiawa.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 会员验证码登录请求 DTO。 */
public class MemberLoginReq {

    /** 手机号，11 位大陆号码格式 */
    @NotBlank(message = "【手机号】不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
    private String mobile;

    /** 短信验证码（开发环境填 8888） */
    @NotBlank(message = "【短信验证码】不能为空")
    private String code;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MemberLoginReq{");
        sb.append("mobile='").append(mobile).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
