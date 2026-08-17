package com.jiawa.train.common.context;

import com.jiawa.train.common.resp.MemberLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 当前登录会员上下文（common 模块）。 */
public class LoginMemberContext {
    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);

    /** 线程级登录会员信息，一请求一线程一份副本 */
    private static ThreadLocal<MemberLoginResp> member = new ThreadLocal<>();

    /** 获取当前线程绑定的登录会员信息，未登录时返回 null */
    public static MemberLoginResp getMember() {
        return member.get();
    }

    /** 将登录会员信息绑定到当前线程（由拦截器调用） */
    public static void setMember(MemberLoginResp member) {
        LoginMemberContext.member.set(member);
    }

    /** 获取当前登录会员 ID，未登录或上下文缺失时抛异常 */
    public static Long getId() {
        try {
            return member.get().getId();
        } catch (Exception e) {
            LOG.error("获取登录会员信息异常", e);
            throw e;
        }
    }

    /**
     * 清理当前线程的会员上下文，防止线程池复用导致的数据泄漏。
     * 必须在拦截器 {@code afterCompletion} 中调用，与 {@link #setMember} 成对出现。
     */
    public static void remove() {
        member.remove();
    }

}
