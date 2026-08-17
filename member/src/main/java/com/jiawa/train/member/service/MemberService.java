package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.JwtUtil;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import com.jiawa.train.member.req.MemberLoginReq;
import com.jiawa.train.member.req.MemberRegisterReq;
import com.jiawa.train.member.req.MemberSendCodeReq;
import com.jiawa.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** 会员核心业务服务。 */
@Service
public class MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    @Resource
    private MemberMapper memberMapper;

    /**
     * 统计会员表总记录数。
     *
     * @return 会员数量
     */
    public int count() {
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    /**
     * 显式注册：手机号唯一，已存在则抛 {@link BusinessExceptionEnum#MEMBER_MOBILE_EXIST}。
     *
     * @param req 注册请求
     * @return 新会员雪花 ID
     */
    public long register(MemberRegisterReq req) {
        String mobile = req.getMobile();
        Member memberDB = selectByMobile(mobile);

        if (ObjectUtil.isNotNull(memberDB)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        Member member = new Member();
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }

    /**
     * 发送短信验证码（开发环境固定 8888）。
     * <p>
     * 核心逻辑：
     * <ol>
     *   <li>按手机号查询会员，不存在则静默注册（自动 insert）</li>
     *   <li>生成验证码（当前硬编码 8888，生产应随机并写入 Redis）</li>
     *   <li>记录短信流水、对接短信通道（本 demo 仅打日志）</li>
     * </ol>
     *
     * @param req 发码请求
     */
    public void sendCode(MemberSendCodeReq req) {
        String mobile = req.getMobile();
        Member memberDB = selectByMobile(mobile);

        // 如果手机号不存在，则插入一条记录
        if (ObjectUtil.isNull(memberDB)) {
            LOG.info("手机号不存在，插入一条记录");
            Member member = new Member();
            member.setId(SnowUtil.getSnowflakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
        } else {
            LOG.info("手机号存在，不插入记录");
        }

        // 开发环境固定验证码；生产应改为随机码 + Redis 过期 + 防刷
        // String code = RandomUtil.randomString(4);
        String code = "8888";
        LOG.info("短信验证码已生成");

        // 保存短信记录表：手机号，短信验证码，有效期，是否已使用，业务类型，发送时间，使用时间
        LOG.info("保存短信记录表");

        // 对接短信通道，发送短信
        LOG.info("对接短信通道");
    }

    /**
     * 验证码登录：校验手机号与验证码，通过后签发 JWT 返回客户端。
     * <p>
     * 注意：必须先调 send-code 完成静默注册，否则手机号不存在会报错。
     * JWT 由网关校验，本服务拦截器解析后供乘车人等接口使用。
     *
     * @param req 登录请求（手机号 + 验证码）
     * @return 会员基本信息 + token
     */
    public MemberLoginResp login(MemberLoginReq req) {
        String mobile = req.getMobile();
        String code = req.getCode();
        Member memberDB = selectByMobile(mobile);

        // 如果手机号不存在，则插入一条记录
        if (ObjectUtil.isNull(memberDB)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        // 开发环境固定校验 8888；生产应从 Redis 比对且一次性失效
        if (!"8888".equals(code)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }

        MemberLoginResp memberLoginResp = BeanUtil.copyProperties(memberDB, MemberLoginResp.class);
        // 签发 JWT：payload 含会员 id、mobile；网关验签，MemberInterceptor 解析入上下文
        String token = JwtUtil.createToken(memberLoginResp.getId(), memberLoginResp.getMobile());
        memberLoginResp.setToken(token);
        return memberLoginResp;
    }

    /**
     * 按手机号查询会员（内部复用）。
     *
     * @param mobile 手机号
     * @return 会员实体，不存在返回 null
     */
    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);
        if (CollUtil.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
