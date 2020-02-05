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
@TableName("t_picture_catalog")
public class PictureCatalog extends BaseEntity<PictureCatalog> {

    /**
     * 父UID
     */
    private String parentUid;

    /**
     * 分类名
     */
    private String name;

    /**
     * 分类图片Uid
     */
    private String fileUid;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;

    //以下字段不存入数据库

    /**
     * 分类图
     */
    @TableField(exist = false)
    private List<String> photoList;

}