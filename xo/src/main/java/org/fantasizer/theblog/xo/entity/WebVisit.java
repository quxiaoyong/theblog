package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

/**
 * 网站访问记录
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:29
 */
@Data
@TableName("t_web_visit")
public class WebVisit extends BaseEntity<WebVisit> {


    /**
     * 用户UID
     */
    private String userUid;

    /**
     * 用户IP
     */
    private String ip;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 用户访问行为   (点击了文章，点击了标签，点击了分类，进行了搜索)
     */
    private String behavior;

    /**
     * 文章uid，标签uid，分类uid
     */
    private String moduleUid;

    /**
     * 附加数据(比如搜索内容)
     */
    private String otherData;


    // 以下字段不存入数据库

    /**
     * 内容(点击的博客名，点击的标签名，搜索的内容，点击的作者)
     */
    @TableField(exist = false)
    private String content;

    /**
     * 行为名称
     */
    @TableField(exist = false)
    private String behaviorContent;
}