package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:25
 */

@Data
@TableName("t_system_log")
public class SystemLog extends BaseEntity<SystemLog> {

    /**
     * 操作用户名
     */
    private String userName;

    /**
     * 操作人uid
     */
    private String adminUid;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求方式 GET POST
     */
    private String type;

    /**
     * 请求类路径
     */
    private String classPath;

    /**
     * 方法名
     */
    private String method;

    /**
     * 参数
     */
    private String params;

    /**
     * 描述
     */
    private String operation;
}