package org.fantasizer.theblog.common.enums;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 21:59
 */
public enum Status {

    /**
     * 删除的
     */
    DELETED(0),

    /**
     * 有效的
     */
    ENABLED(1),

    /**
     * 冻结的
     */
    FREEZE(2),


    /**
     * 置顶的
     */
    STICK(3);

    private int value;

    public  int getValue(){
        return this.value;
    }

    public static int getValue(Status status){
        return status.getValue();
    }

    Status(int value) {
        this.value = value;
    }
}
