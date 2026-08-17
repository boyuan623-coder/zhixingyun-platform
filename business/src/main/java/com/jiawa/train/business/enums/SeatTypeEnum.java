package com.jiawa.train.business.enums;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/** 席别枚举 —— 座位类型、中文名、里程单价（元/公里）。 */
public enum SeatTypeEnum {

    /** 一等座，0.4 元/公里 */
    YDZ("1", "一等座", new BigDecimal("0.4")),
    /** 二等座，0.3 元/公里 */
    EDZ("2", "二等座", new BigDecimal("0.3")),
    /** 软卧，0.6 元/公里 */
    RW("3", "软卧", new BigDecimal("0.6")),
    /** 硬卧，0.5 元/公里 */
    YW("4", "硬卧", new BigDecimal("0.5"));

    private String code;

    private String desc;

    /**
     * 基础票价 N元/公里，0.4即为0.4元/公里
     */
    private BigDecimal price;

    SeatTypeEnum(String code, String desc, BigDecimal price) {
        this.code = code;
        this.desc = desc;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public static List<HashMap<String,String>> getEnumList() {
        List<HashMap<String, String>> list = new ArrayList<>();
        for (SeatTypeEnum anEnum : EnumSet.allOf(SeatTypeEnum.class)) {
            HashMap<String, String> map = new HashMap<>();
            map.put("code",anEnum.code);
            map.put("desc",anEnum.desc);
            list.add(map);
        }
        return list;
    }

    public static SeatTypeEnum getEnumByCode(String code) {
        for (SeatTypeEnum enums : SeatTypeEnum.values()) {
            if (enums.getCode().equalsIgnoreCase(code)) {
                return enums;
            }
        }
        return null;
    }
}
