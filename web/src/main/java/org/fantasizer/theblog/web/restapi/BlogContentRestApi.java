package org.fantasizer.theblog.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.common.enums.Behavior;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.helper.IpUtils;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.helper.WebUtils;
import org.fantasizer.theblog.web.feign.PictureFeignClient;
import org.fantasizer.theblog.web.global.SQLConfiguration;
import org.fantasizer.theblog.web.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.Blog;
import org.fantasizer.theblog.xo.entity.WebVisit;
import org.fantasizer.theblog.xo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/content")
@Api(value = "文章详情RestApi", tags = {"BlogContentRestApi"})
public class BlogContentRestApi {

    private static Logger log = LogManager.getLogger(BlogContentRestApi.class);
    @Autowired
    TagService tagService;

    @Autowired
    BlogCatalogService blogCatalogService;

    @Autowired
    LinkService linkService;
    @Autowired
    private BlogService blogService;
    @Autowired
    private PictureFeignClient pictureFeignClient;
    @Autowired
    private WebVisitService webVisitService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation(value = "通过Uid获取博客内容", notes = "通过Uid获取博客内容")
    @GetMapping("/getBlogByUid")
    public String getBlogByUid(HttpServletRequest request,
                               @ApiParam(name = "uid", value = "博客UID", required = false) @RequestParam(name = "uid", required = false) String uid) {

        String ip = IpUtils.getIpAddr(request);

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "UID不能为空");
        }

        Blog blog = blogService.getById(uid);


        if (blog != null) {

            // 设置文章版权申明
            blogService.setBlogCopyright(blog);

            //设置博客标签
            blogService.setTagByBlog(blog);

            //获取分类
            blogService.setCatalogByBlog(blog);

            //设置博客标题图
            setPhotoListByBlog(blog);

            //从Redis取出数据，判断该用户是否点击过
            String jsonResult = stringRedisTemplate.opsForValue().get("BLOG_CLICK:" + ip + "#" + uid);

            //从Redis取出用户点赞数据
            String pariseJsonResult = stringRedisTemplate.opsForValue().get("BLOG_PRAISE:" + uid);

            if (StringUtils.isEmpty(jsonResult)) {

                //给博客点击数增加
                Integer clickCount = blog.getClickCount() + 1;
                blog.setClickCount(clickCount);
                blog.updateById();

                //将该用户点击记录存储到redis中, 24小时后过期
                stringRedisTemplate.opsForValue().set("BLOG_CLICK:" + ip + "#" + uid, blog.getClickCount().toString(),
                        24, TimeUnit.HOURS);
            }

            if (!StringUtils.isEmpty(pariseJsonResult)) {
                Integer pariseCount = Integer.parseInt(pariseJsonResult);
                blog.setPraiseCount(pariseCount);
            } else {
                blog.setPraiseCount(0);
            }

            //增加记录（可以考虑使用AOP）
            webVisitService.addWebVisit(null, request, Behavior.BLOG_CONTNET.getBehavior(), blog.getUid(), null);

        }

        log.info("返回结果");
        return ResultUtil.result(SystemConfiguration.SUCCESS, blog);
    }

    @ApiOperation(value = "通过Uid获取博客点赞数", notes = "通过Uid获取博客点赞数")
    @GetMapping("/getBlogPraiseCountByUid")
    public String getBlogPraiseCountByUid(HttpServletRequest request,
                                          @ApiParam(name = "uid", value = "博客UID", required = false) @RequestParam(name = "uid", required = false) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "UID不能为空");
        }

        //从Redis取出用户点赞数据
        String pariseJsonResult = stringRedisTemplate.opsForValue().get("BLOG_PRAISE:" + uid);
        Integer pariseCount = 0;
        if (!StringUtils.isEmpty(pariseJsonResult)) {
            pariseCount = Integer.parseInt(pariseJsonResult);
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, pariseCount);
    }

    @ApiOperation(value = "通过Uid给博客点赞", notes = "通过Uid给博客点赞")
    @GetMapping("/praiseBlogByUid")
    public String praiseBlogByUid(HttpServletRequest request,
                                  @ApiParam(name = "uid", value = "博客UID", required = false) @RequestParam(name = "uid", required = false) String uid) {

        String ip = IpUtils.getIpAddr(request);

        //判断该IP是否点赞过
        QueryWrapper<WebVisit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.IP, ip);
        queryWrapper.eq(SQLConfiguration.MODULE_UID, uid);
        queryWrapper.eq(SQLConfiguration.BEHAVIOR, Behavior.BLOG_PRAISE);
        WebVisit webVisit = webVisitService.getOne(queryWrapper);
        if (webVisit != null) {
            return ResultUtil.result(SystemConfiguration.ERROR, "您已经点过赞了！");
        }

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "UID不能为空");
        }

        Blog blog = blogService.getById(uid);

        //从Redis取出数据，判断该用户是否点击过
        String pariseJsonResult = stringRedisTemplate.opsForValue().get("BLOG_PRAISE:" + uid);

        if (StringUtils.isEmpty(pariseJsonResult)) {

            //给该博客点赞数置为1
            stringRedisTemplate.opsForValue().set("BLOG_PRAISE:" + uid, "1");

            blog.setCollectCount(1);
            blog.updateById();

        } else {
            Integer count = Integer.valueOf(pariseJsonResult) + 1;

            //给该博客点赞 +1
            stringRedisTemplate.opsForValue().set("BLOG_PRAISE:" + uid, count.toString());

            blog.setCollectCount(count);
            blog.updateById();
        }

        //增加记录（可以考虑使用AOP）
        webVisitService.addWebVisit(null, request, Behavior.BLOG_PRAISE.getBehavior(), blog.getUid(), null);

        return ResultUtil.result(SystemConfiguration.SUCCESS, "");
    }

    @ApiOperation(value = "根据标签Uid获取相关的博客", notes = "根据标签获取相关的博客")
    @GetMapping("/getSameBlogByTagUid")
    public String getSameBlogByTagUid(HttpServletRequest request,
                                      @ApiParam(name = "tagUid", value = "博客标签UID", required = true) @RequestParam(name = "tagUid", required = true) String tagUid,
                                      @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                      @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        if (StringUtils.isEmpty(tagUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "标签UID不能为空");
        }

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.TagUid, tagUid);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();
        for (Blog item : list) {
            //获取标签
            blogService.setTagByBlog(item);
            //获取分类
            blogService.setCatalogByBlog(item);
            //设置博客标题图
            setPhotoListByBlog(item);
        }
        log.info("返回结果");
        pageList.setRecords(list);
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "根据BlogUid获取相关的博客", notes = "根据BlogUid获取相关的博客")
    @GetMapping("/getSameBlogByBlogUid")
    public String getSameBlogByBlogUid(HttpServletRequest request,
                                       @ApiParam(name = "blogUid", value = "博客标签UID", required = true) @RequestParam(name = "blogUid", required = true) String blogUid,
                                       @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                       @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        if (StringUtils.isEmpty(blogUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "博客UID不能为空");
        }

        Blog blog = blogService.getById(blogUid);

        if (blog == null || blog.getStatus() == Status.DELETED) {
            return ResultUtil.result(SystemConfiguration.ERROR, "该博客不存在");
        }

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        // 因为tagUid可能存在多个，需要切割进行拼接操作
        List<String> tagList = StringUtils.changeStringToString(blog.getTagUid(), ",");
        for (int a = 0; a < tagList.size(); a++) {
            if (a < tagList.size() - 1) {
                queryWrapper.eq(SQLConfiguration.TagUid, tagList.get(a)).or();
            } else {
                queryWrapper.eq(SQLConfiguration.TagUid, tagList.get(a));
            }
        }

        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);

        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();
        for (Blog item : list) {
            //获取标签
            blogService.setTagByBlog(item);
            //获取分类
            blogService.setCatalogByBlog(item);
            //设置博客标题图
            setPhotoListByBlog(item);
        }

        //过滤掉当前的博客
        List<Blog> newList = new ArrayList<>();
        for (Blog item : list) {
            if (item.getUid().equals(blogUid)) {
                continue;
            }
            newList.add(item);
        }

        log.info("返回结果");
        pageList.setRecords(newList);
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    /**
     * 设置博客标题图
     *
     * @param blog
     */
    private void setPhotoListByBlog(Blog blog) {
        //获取标题图片
        if (blog != null && !StringUtils.isEmpty(blog.getFileUid())) {
            String result = this.pictureFeignClient.getPicture(blog.getFileUid(), ",");
            List<String> picList = WebUtils.getPicture(result);
            log.info("##### picList: #######" + picList);
            if (picList != null && picList.size() > 0) {
                blog.setPhotoList(picList);
            }
        }
    }
}

