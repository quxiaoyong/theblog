package org.fantasizer.theblog.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Behavior;
import org.fantasizer.common.enums.Publish;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.global.BasicSQLConfiguration;
import org.fantasizer.common.helper.JsonHelper;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.theblog.web.global.MessageConfiguration;
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
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/sort")
@Api(value = "归档 RestApi", tags = {"SortRestApi"})
public class CatalogRestApi {

    private static Logger log = LogManager.getLogger(CatalogRestApi.class);
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
     * 获取归档的信息
     *
     * @author xzx19950624@qq.com
     * @date 2018年11月6日下午8:57:48
     */

    @ApiOperation(value = "归档", notes = "归档")
    @GetMapping("/getSortList")
    public String getSortList(HttpServletRequest request) {

        //从Redis中获取内容
        String monthResult = stringRedisTemplate.opsForValue().get(SystemConfiguration.MONTH_SET);

        //判断redis中时候包含归档的内容
        if (StringUtils.isNotEmpty(monthResult)) {
            List list = JsonHelper.jsonArrayToArrayList(monthResult);
            return ResultUtil.result(SystemConfiguration.SUCCESS, list);
        }

        // 第一次启动的时候归档
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        queryWrapper.eq(SQLConfiguration.IS_PUBLISH, Publish.PUBLISH);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));
        List<Blog> list = blogService.list(queryWrapper);

        //给博客增加标签和分类
        list = setBlog(list);

        Map<String, List<Blog>> map = new HashMap<>();
        Iterator iterable = list.iterator();
        Set<String> monthSet = new TreeSet<>();
        while (iterable.hasNext()) {
            Blog blog = (Blog) iterable.next();
            Date createTime = blog.getCreateTime();

            String month = new SimpleDateFormat("yyyy年MM月").format(createTime).toString();

            monthSet.add(month);

            if (map.get(month) == null) {
                List<Blog> blogList = new ArrayList<>();
                blogList.add(blog);
                map.put(month, blogList);
            } else {
                List<Blog> blogList = map.get(month);
                blogList.add(blog);
                map.put(month, blogList);

            }
        }

        // 缓存该月份下的所有文章  key: 月份   value：月份下的所有文章
        map.forEach((key, value) -> {
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_SORT_BY_MONTH + SystemConfiguration.REDIS_SEGMENTATION + key, JsonHelper.objectToJson(value).toString());
        });

        //将从数据库查询的数据缓存到redis中
        stringRedisTemplate.opsForValue().set(SystemConfiguration.MONTH_SET, JsonHelper.objectToJson(monthSet).toString());

        return ResultUtil.result(SystemConfiguration.SUCCESS, monthSet);
    }

    @ApiOperation(value = "通过月份获取文章", notes = "通过月份获取文章")
    @GetMapping("/getArticleByMonth")
    public String getArticleByMonth(HttpServletRequest request,
                                    @ApiParam(name = "monthDate", value = "归档的日期", required = false) @RequestParam(name = "monthDate", required = false) String monthDate) {

        if (StringUtils.isEmpty(monthDate)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }

        //增加点击记录
        webVisitService.addWebVisit(null, request, Behavior.VISIT_SORT.getBehavior(), null, monthDate);

        //从Redis中获取内容
        String contentResult = stringRedisTemplate.opsForValue().get(SystemConfiguration.BLOG_SORT_BY_MONTH + SystemConfiguration.REDIS_SEGMENTATION + monthDate);

        //判断redis中时候包含该日期下的文章
        if (StringUtils.isNotEmpty(contentResult)) {
            List list = JsonHelper.jsonArrayToArrayList(contentResult);
            return ResultUtil.result(SystemConfiguration.SUCCESS, list);
        }

        // 第一次启动的时候归档
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));
        List<Blog> list = blogService.list(queryWrapper);

        //给博客增加标签和分类
        list = setBlog(list);

        Map<String, List<Blog>> map = new HashMap<>();
        Iterator iterable = list.iterator();
        Set<String> monthSet = new TreeSet<>();
        while (iterable.hasNext()) {
            Blog blog = (Blog) iterable.next();
            Date createTime = blog.getCreateTime();

            String month = new SimpleDateFormat("yyyy年MM月").format(createTime).toString();

            monthSet.add(month);

            if (map.get(month) == null) {
                List<Blog> blogList = new ArrayList<>();
                blogList.add(blog);
                map.put(month, blogList);
            } else {
                List<Blog> blogList = map.get(month);
                blogList.add(blog);
                map.put(month, blogList);
            }
        }

        // 缓存该月份下的所有文章  key: 月份   value：月份下的所有文章
        map.forEach((key, value) -> {
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_SORT_BY_MONTH + SystemConfiguration.REDIS_SEGMENTATION + key, JsonHelper.objectToJson(value).toString());
        });

        //将从数据库查询的数据缓存到redis中
        stringRedisTemplate.opsForValue().set(SystemConfiguration.MONTH_SET, JsonHelper.objectToJson(monthSet).toString());

        return ResultUtil.result(SystemConfiguration.SUCCESS, map.get(monthDate));
    }

    /**
     * 设置博客的分类标签和分类
     *
     * @param list
     * @return
     */
    private List<Blog> setBlog(List<Blog> list) {
        final StringBuffer fileUids = new StringBuffer();
        List<String> sortUids = new ArrayList<String>();
        List<String> tagUids = new ArrayList<String>();

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
                List<Tag> tagListTemp = new ArrayList<Tag>();
                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }
        }
        return list;
    }

}

