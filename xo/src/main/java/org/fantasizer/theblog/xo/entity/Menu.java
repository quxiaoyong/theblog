package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

import java.util.List;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:17
 */
@Data
@TableName("t_menu")
public class Menu extends BaseEntity<Menu> implements Comparable<Menu> {

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单级别 （一级分类，二级分类）
     */
    private Integer menuLevel;

    /**
     * 介绍
     */
    private String summary;

    /**
     * Icon图标
     */
    private String icon;

    /**
     * 父UID
     */
    private String parentUid;

    /**
     * URL地址
     */
    private String url;

    /**
     * 排序字段(越大越靠前)
     */
    private Integer sort;

    /**
     * 父菜单
     */
    @TableField(exist = false)
    private Menu parentCategoryMenu;

    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<Menu> childCategoryMenu;

    @Override
    public int compareTo(Menu o) {

        if (this.sort >= o.getSort()) {
            return -1;
        }
        return 1;
    }
}
