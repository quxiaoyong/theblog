package org.fantasizer.common.enums;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 22:00
 */
public enum SystemLanguageEnum {
    /**
     * 中文
     */
    ZH("zh", "中文"),

    /**
     * 英文
     */
    EN("en", "英文");

    private final String code;
    private final String name;

    SystemLanguageEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
