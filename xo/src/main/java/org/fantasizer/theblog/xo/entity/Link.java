package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.common.entity.BaseEntity;

/**
 * 友情链接
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:21
 */
@Data
@TableName("t_link")
public class Link extends BaseEntity<Link> {

    private static final long serialVersionUID = 1L;


    /**
     * 友链标题
     */
    private String title;

    /**
     * 友链介绍
     */
    private String summary;

    /**
     * 友链地址
     */
    private String url;

    /**
     * 点击数
     */
    private Integer clickCount;

    /**
     * 排序字段
     */
    private Integer sort;
}
