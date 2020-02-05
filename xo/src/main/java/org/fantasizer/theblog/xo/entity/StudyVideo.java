package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

import java.util.List;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:24
 */
@Data
@TableName("t_study_video")
public class StudyVideo extends BaseEntity<StudyVideo> {

    /**
     * 视频名称
     */
    private String name;

    /**
     * 视频简介
     */
    private String summary;

    /**
     * 视频内容介绍
     */
    private String content;

    /**
     * TODO:以后修改自存储视频
     *
     * 百度云完整路径
     */
    private String baiduPath;

    /**
     * 视频封面图片UID
     */
    private String fileUid;

    /**
     * 资源分类UID
     */
    private String resourceSortUid;

    /**
     * 点击数
     */
    private String clickCount;

    /**
     * 学习视频标题图
     */
    @TableField(exist = false)
    private List<String> photoList;

    /**
     * 资源分类
     */
    @TableField(exist = false)
    private ResourceCatalog resourceCatalog;
}