package com.jiawa.train.common.util;

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

/** JWT 工具类（common 模块）。 */
public class JwtUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);

    /** HMAC 签名密钥，须与 gateway 模块保持一致；生产请改为配置/环境变量。 */
    private static final String key = "train-jwt-secret-dev";

    /**
     * 签发 JWT token，有效期 24 小时，载荷含会员 id 和 mobile。
     *
     * @param id     会员 ID
     * @param mobile 手机号
     * @return 签名字符串 token
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
     * 校验 token 签名与有效期，异常或过期返回 false。
     *
     * @param token JWT 字符串
     * @return 有效 true，无效 false
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
     * 解析 token 载荷为 JSONObject，去除 iat/exp/nbf 标准字段，保留 id、mobile 等业务字段。
     *
     * @param token JWT 字符串（调用方应确保已通过 validate）
     * @return 业务载荷 JSON
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
