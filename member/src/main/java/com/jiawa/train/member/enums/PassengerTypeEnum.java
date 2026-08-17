package com.jiawa.train.member.enums;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/** 乘车人类型枚举。 */
public enum PassengerTypeEnum {

    /** 成人票 */
    ADULT("1", "成人"),
    /** 儿童票 */
    CHILD("2", "儿童"),
    /** 学生票 */
    STUDENT("3", "学生");

    /** 类型码值，存入 passenger.type */
    private String code;

    /** 类型中文描述，用于前端展示 */
    private String desc;

    PassengerTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 将枚举转为前端友好的 List&lt;Map&gt; 结构（code + desc）。
     *
     * @return 枚举字典列表
     */
    public static List<HashMap<String,String>> getEnumList() {
        List<HashMap<String, String>> list = new ArrayList<>();
        for (PassengerTypeEnum anEnum : EnumSet.allOf(PassengerTypeEnum.class)) {
            HashMap<String, String> map = new HashMap<>();
            map.put("code",anEnum.code);
            map.put("desc",anEnum.desc);
            list.add(map);
        }
        return list;
    }
}
