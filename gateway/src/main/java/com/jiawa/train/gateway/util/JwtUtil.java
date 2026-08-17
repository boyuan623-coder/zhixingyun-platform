package com.jiawa.train.gateway.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** JWT 工具类（gateway 模块）。 */
public class JwtUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);

    /** HMAC 签名密钥，须与 common 模块保持一致；生产请改为配置/环境变量。 */
    private static final String key = "train-jwt-secret-dev";

    /**
     * 签发 JWT token（网关模块保留此方法便于本地测试，生产签发在 member 服务）。
     */
    public static String createToken(Long id, String mobile) {
        DateTime now = DateTime.now();
        DateTime expTime = now.offsetNew(DateField.HOUR, 24);
        Map<String, Object> payload = new HashMap<>();
        // 签发时间
        payload.put(JWTPayload.ISSUED_AT, now);
        // 过期时间
        payload.put(JWTPayload.EXPIRES_AT, expTime);
        // 生效时间
        payload.put(JWTPayload.NOT_BEFORE, now);
        // 内容
        payload.put("id", id);
        payload.put("mobile", mobile);
        String token = JWTUtil.createToken(payload, key.getBytes());
        LOG.info("JWT token 已签发");
        return token;
    }

    /**
     * 校验 token 签名与有效期——LoginMemberFilter 的核心调用。
     *
     * @param token 请求 Header 中的 JWT
     * @return 有效 true，无效或异常 false
     */
    public static boolean validate(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token).setKey(key.getBytes());
            // validate包含了verify
            boolean validate = jwt.validate(0);
            LOG.debug("JWT token 校验结果：{}", validate);
            return validate;
        } catch (Exception e) {
            LOG.error("JWT token校验异常", e);
            return false;
        }
    }

    /**
     * 解析 token 业务载荷（网关鉴权场景通常不需要，保留与 common 模块对齐）。
     */
    public static JSONObject getJSONObject(String token) {
        JWT jwt = JWTUtil.parseToken(token).setKey(key.getBytes());
        JSONObject payloads = jwt.getPayloads();
        payloads.remove(JWTPayload.ISSUED_AT);
        payloads.remove(JWTPayload.EXPIRES_AT);
        payloads.remove(JWTPayload.NOT_BEFORE);
        LOG.debug("JWT payload 已解析");
        return payloads;
    }
}
