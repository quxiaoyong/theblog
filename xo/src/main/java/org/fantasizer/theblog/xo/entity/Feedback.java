package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 19:20
 */
@Data
@TableName("t_feedback")
public class Feedback extends BaseEntity<Feedback> {

    /**
     * 用户uid
     */
    private String userUid;

    /**
     * 反馈的内容
     */
    private String content;

}