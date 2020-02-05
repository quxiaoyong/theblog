package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

import java.util.Date;

/**
 * 访客
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:28
 */
@Data
@TableName("t_visitor")
public class Visitor extends BaseEntity<Visitor> {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;
}