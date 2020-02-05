package org.fantasizer.theblog.xo.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.fantasizer.theblog.common.global.BasicSQLConfiguration;
import org.fantasizer.theblog.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.xo.entity.Comment;
import org.fantasizer.theblog.xo.mapper.CommentMapper;
import org.fantasizer.theblog.xo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author Cruise Qu
 * @Date 2020-01-30 20:02
 */
@Service


public class CommentServiceImpl extends BaseServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    CommentMapper commentMapper;

    @Override
    public Integer getCommentCount(int status) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(BasicSQLConfiguration.STATUS, status);
        return commentMapper.selectCount(queryWrapper);
    }

}
