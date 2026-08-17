package com.jiawa.train.member.req;

import com.jiawa.train.common.req.PageReq;

/** 乘车人分页查询请求 DTO。 */
public class PassengerQueryReq extends PageReq {

    /** 会员 ID，Controller 层从 LoginMemberContext 设置，用于过滤本会员乘车人 */
    private Long memberId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PassengerQueryReq{");
        sb.append("memberId=").append(memberId);
        sb.append('}');
        return sb.toString();
    }
}
