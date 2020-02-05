package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:26
 */
@Data
@TableName("t_tag")
public class Tag extends BaseEntity<Tag> {

    private static final long serialVersionUID = 1L;

    /**
     * 标签内容
     */
    private String content;

    /**
     * 标签简介
     */
    private int clickCount;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;
}
