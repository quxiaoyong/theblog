package org.fantasizer.theblog.common.enums;

/**
 * 推荐等级枚举类
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:57
 */
public enum Level {

    /**
     * 正常呢
     */
    NORMAL(0),

    /**
     * 一级推荐
     */
    FIRST(1),

    SECOND(2),

    THIRD(3),

    FOURTH(4);

    private int value;

    public int getValue(){
        return this.value;
    }

    Level(int value) {
        this.value = value;
    }
    }
