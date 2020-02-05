package org.fantasizer.theblog.xo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.fantasizer.theblog.common.entity.BaseEntity;

/**
 * 待办事项
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:26
 */
@Data
@TableName("t_todo")
public class Todo extends BaseEntity<Todo> {

    /**
     * 内容
     */
    private String text;

    /**
     * 管理员UID
     */
    private String adminUid;

    /**
     * 表示事项是否完成
     */
    private Boolean done;
}