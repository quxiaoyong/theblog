package org.fantasizer.theblog.xo.service;

import org.fantasizer.common.service.BaseService;
import org.fantasizer.theblog.xo.entity.Comment;

/**
 * 评论服务
 *
 * @Author Cruise Qu
 * @Date 2020-01-30 19:50
 */
public interface CommentService extends BaseService<Comment> {

    /**
     * 获取评论数目
     *
     * @author xzx19950624@qq.com
     * @date 2018年10月22日下午3:43:38
     */
    Integer getCommentCount(int status);

}
