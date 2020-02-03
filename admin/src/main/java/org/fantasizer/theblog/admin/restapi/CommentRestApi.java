package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.Comment;
import org.fantasizer.theblog.xo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(value = "评论RestApi", tags = {"CommentRestApi"})
@RestController
@RequestMapping("/comment")
public class CommentRestApi {
    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    CommentService commentService;

    @ApiOperation(value = "获取评论列表", notes = "获取评论列表", response = String.class)
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public String getList(HttpServletRequest request,
                          @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<Comment>();
        if (StringUtils.isNotEmpty(keyword) && !StringUtils.isEmpty(keyword.trim())) {
            queryWrapper.like(SQLConfiguration.CONTENT, keyword.trim());
        }

        Page<Comment> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        IPage<Comment> pageList = commentService.page(page, queryWrapper);
        log.info("返回结果");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "增加评论", notes = "增加评论", response = String.class)
    @PostMapping("/add")
    public String add(@RequestBody Comment comment) {

        if (StringUtils.isEmpty(comment.getUserUid()) || StringUtils.isEmpty(comment.getContent())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        comment.setStatus(Status.ENABLED);
        comment.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "添加成功");
    }

    @ApiOperation(value = "编辑评论", notes = "编辑评论", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request, @RequestBody Comment comment) {

        if (StringUtils.isEmpty(comment.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        comment.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "编辑成功");
    }

    @ApiOperation(value = "删除评论", notes = "删除评论", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        Comment comment = commentService.getById(uid);
        comment.setStatus(Status.DELETED);
        comment.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "删除成功");
    }
}

