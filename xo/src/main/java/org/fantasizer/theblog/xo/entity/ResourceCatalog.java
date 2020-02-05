package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

import java.util.List;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:22
 */

@Data
@TableName("t_resource_catalog")
public class ResourceCatalog extends BaseEntity<ResourceCatalog> {

    private static final long serialVersionUID = 1L;


    /**
     * 分类名
     */
    private String sortName;

    /**
     * 分类介绍
     */
    private String content;

    /**
     * 分类图片UID
     */
    private String fileUid;

    /**
     * 分类点击数
     */
    private String clickCount;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;

    /**
     * 分类图
     */
    @TableField(exist = false)
    private List<String> photoList;
}