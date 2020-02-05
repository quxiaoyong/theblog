package org.fantasizer.theblog.admin.restapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.xo.service.BlogService;
import org.fantasizer.theblog.xo.service.CommentService;
import org.fantasizer.theblog.xo.service.WebVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/index")
@Api(value = "首页RestApi", tags = {"IndexRestApi"})
public class IndexRestApi {

    @Autowired
    BlogService blogService;

    @Autowired
    CommentService commentService;

    @Autowired
    WebVisitService webVisitService;

    @ApiOperation(value = "首页初始化数据", notes = "首页初始化数据", response = String.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String init() {
        Map<String, Object> map = new HashMap<>();

        Integer blogCount = blogService.getBlogCount(Status.ENABLED.getValue());
        Integer commentCount = commentService.getCommentCount(Status.ENABLED.getValue());
        Integer visitCount = webVisitService.getWebVisitCount();

        map.put(SystemConfiguration.BLOG_COUNT, blogCount);
        map.put(SystemConfiguration.COMMENT_COUNT, commentCount);
        map.put(SystemConfiguration.VISIT_COUNT, visitCount);

        return ResultUtil.result(SystemConfiguration.SUCCESS, map);
    }

    @ApiOperation(value = "获取最近一周用户独立IP数和访问量", notes = "获取最近一周用户独立IP数和访问量", response = String.class)
    @RequestMapping(value = "/getVisitByWeek", method = RequestMethod.GET)
    public String getVisitByWeek() {

        Map<String, Object> visitByWeek = webVisitService.getVisitByWeek();

        return ResultUtil.result(SystemConfiguration.SUCCESS, visitByWeek);
    }

    @ApiOperation(value = "获取每个标签下文章数目", notes = "获取每个标签下文章数目", response = String.class)
    @RequestMapping(value = "/getBlogCountByTag", method = RequestMethod.GET)
    public String getBlogCountByTag() {

        List<Map<String, Object>> blogCountByTag = blogService.getBlogCountByTag();

        return ResultUtil.result(SystemConfiguration.SUCCESS, blogCountByTag);
    }

    @ApiOperation(value = "获取每个分类下文章数目", notes = "获取每个分类下文章数目", response = String.class)
    @RequestMapping(value = "/getBlogCountByBlogSort", method = RequestMethod.GET)
    public String getBlogCountByBlogSort() {

        List<Map<String, Object>> blogCountByTag = blogService.getBlogCountByBlogCatalog();

        return ResultUtil.result(SystemConfiguration.SUCCESS, blogCountByTag);
    }

    @ApiOperation(value = "获取一年内的文章贡献数", notes = "获取一年内的文章贡献度", response = String.class)
    @RequestMapping(value = "/getBlogContributeCount", method = RequestMethod.GET)
    public String getBlogContributeCount() {

        Map<String, Object> resultMap = blogService.getBlogContributeCount();
        return ResultUtil.result(SystemConfiguration.SUCCESS, resultMap);
    }


}
