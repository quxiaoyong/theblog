package org.fantasizer.theblog.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Behavior;
import org.fantasizer.common.enums.Publish;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.global.BasicSQLConfiguration;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.theblog.web.global.SQLConfiguration;
import org.fantasizer.theblog.web.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.Blog;
import org.fantasizer.theblog.xo.entity.BlogCatalog;
import org.fantasizer.theblog.xo.entity.Tag;
import org.fantasizer.theblog.xo.service.BlogCatalogService;
import org.fantasizer.theblog.xo.service.BlogService;
import org.fantasizer.theblog.xo.service.TagService;
import org.fantasizer.theblog.xo.service.WebVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/classify")
@Api(value = "分类RestApi", tags = {"ClassifyRestApi"})
public class ClassifyRestApi {

    private static Logger log = LogManager.getLogger(ClassifyRestApi.class);
    @Autowired
    BlogService blogService;
    @Autowired
    TagService tagService;
    @Autowired
    BlogCatalogService blogCatalogService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WebVisitService webVisitService;

    /**
     * 获取分类的信息
     *
     * @author xzx19950624@qq.com
     * @date 2018年11月6日下午8:57:48
     */

    @ApiOperation(value = "获取分类的信息", notes = "获取分类的信息")
    @GetMapping("/getBlogSortList")
    public String getBlogSortList(HttpServletRequest request) {

        QueryWrapper<BlogCatalog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SystemConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        List<BlogCatalog> blogSortList = blogCatalogService.list(queryWrapper);
        return ResultUtil.result(SystemConfiguration.SUCCESS, blogSortList);
    }

    @ApiOperation(value = "通过blogUid获取文章", notes = "通过blogUid获取文章")
    @GetMapping("/getArticleByBlogSortUid")
    public String getArticleByBlogSortUid(HttpServletRequest request,
                                          @ApiParam(name = "blogSortUid", value = "分类UID", required = false) @RequestParam(name = "blogSortUid", required = false) String blogSortUid,
                                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(blogSortUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "传入BlogUid不能为空");
        }

        //增加点击记录
        BlogCatalog blogSort = blogCatalogService.getById(blogSortUid);
        if (blogSort == null) {
            return ResultUtil.result(SystemConfiguration.ERROR, "BlogSort不存在");
        }
        webVisitService.addWebVisit(null, request, Behavior.VISIT_CLASSIFY.getBehavior(), blogSort.getUid(), blogSort.getCatalogName());

        //分页
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        queryWrapper.eq(SQLConfiguration.BLOG_SORT_UID, blogSortUid);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));
        IPage<Blog> pageList = blogService.page(page, queryWrapper);

        //给博客增加标签和分类
        List<Blog> list = setBlog(pageList.getRecords());
        pageList.setRecords(list);

        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    /**
     * 设置博客的分类标签和分类
     *
     * @param list
     * @return
     */
    private List<Blog> setBlog(List<Blog> list) {
        final StringBuffer fileUids = new StringBuffer();
        List<String> sortUids = new ArrayList<>();
        List<String> tagUids = new ArrayList<>();

        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUids.append(item.getFileUid() + SystemConfiguration.FILE_SEGMENTATION);
            }
            if (StringUtils.isNotEmpty(item.getBlogSortUid())) {
                sortUids.add(item.getBlogSortUid());
            }
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                tagUids.add(item.getTagUid());
            }
        });

        Collection<BlogCatalog> sortList = new ArrayList<>();
        Collection<Tag> tagList = new ArrayList<>();
        if (sortUids.size() > 0) {
            sortList = blogCatalogService.listByIds(sortUids);
        }
        if (tagUids.size() > 0) {
            tagList = tagService.listByIds(tagUids);
        }

        Map<String, BlogCatalog> sortMap = new HashMap<>();
        Map<String, Tag> tagMap = new HashMap<>();
        Map<String, String> pictureMap = new HashMap<>();

        sortList.forEach(item -> {
            sortMap.put(item.getUid(), item);
        });

        tagList.forEach(item -> {
            tagMap.put(item.getUid(), item);
        });

        for (Blog item : list) {

            //设置分类
            if (StringUtils.isNotEmpty(item.getBlogSortUid())) {
                item.setBlogCatalog(sortMap.get(item.getBlogSortUid()));
            }
            //获取标签
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SystemConfiguration.FILE_SEGMENTATION);
                List<Tag> tagListTemp = new ArrayList<>();
                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }
        }
        return list;
    }

}

