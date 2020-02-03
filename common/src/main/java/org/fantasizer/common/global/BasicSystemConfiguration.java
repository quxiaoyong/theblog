package org.fantasizer.common.global;

/**
 * TODO:常量类是否可以考虑合并下
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:06
 */
public class BasicSystemConfiguration {

    public final static String SUCCESS = "success";
    public final static String ERROR = "error";
    public final static String STATUS = "status";
    public final static String CREATE_TIME = "createTime";
    public final static String TOKEN = "token";
    public final static String ACCESS_TOKEN = "accessToken";


    public final static String CODE = "code";
    public final static String DATA = "data";
    public final static String UID = "uid";

    /**
     * blog
     */
    public final static String BLOG_UID = "blogUid";
    public final static String LEVEL = "level";


    /**
     * RabbitMQ的命令操作
     */
    public final static String COMMAND = "command";
    public final static String EDIT = "edit";
    public final static String ADD = "add";
    public final static String DELETE = "delete";
    public final static String DELETE_BATCH = "deleteBatch";
    public final static String DELETE_ALL = "deleteAll";
    public final static String EXCHANGE_DIRECT = "exchange.direct";
    public final static String MOGU_BLOG = "mogu.blog";

    // redis相关
    public final static String BLOG_SORT_BY_MONTH = "BLOG_SORT_BY_MONTH";
    // redis分割符
    public final static String REDIS_SEGMENTATION = ":";
    // 月份集合
    public final static String MONTH_SET = "MONTH_SET";
    // 博客等级
    public final static String BLOG_LEVEL = "BLOG_LEVEL";
    // 最热博客
    public final static String HOT_BLOG = "HOT_BLOG";
    // 最新博客
    public final static String NEW_BLOG = "NEW_BLOG";

    // token令牌
    public final static String USER_TOEKN = "userToken";

    // 文件分割符
    public final static String FILE_SEGMENTATION = ",";

    // 系统全局是否标识
    public static final int YES = 1;
    public static final int NO = 0;

    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int FIVE = 5;
    public static final int SIX = 6;
    public static final int SEVEN = 7;
    public static final int EIGHT = 8;
    public static final int NINE = 9;
    public static final int TEN = 10;

}
