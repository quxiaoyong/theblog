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
import org.fantasizer.theblog.common.enums.Publish;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.global.BasicSQLConfiguration;
import org.fantasizer.theblog.common.helper.JsonHelper;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.helper.WebUtils;
import org.fantasizer.theblog.web.feign.PictureFeignClient;
import org.fantasizer.theblog.web.global.MessageConfiguration;
import org.fantasizer.theblog.web.global.SQLConfiguration;
import org.fantasizer.theblog.web.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.*;
import org.fantasizer.theblog.xo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/index")
@Api(value = "首页RestApi", tags = {"IndexRestApi"})
public class IndexRestApi {

    private static Logger log = LogManager.getLogger(IndexRestApi.class);
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
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WebConfigService webConfigService;
    @Autowired
    private WebVisitService webVisitService;
    @Value(value = "${BLOG.HOT_COUNT}")
    private Integer BLOG_HOT_COUNT;
    @Value(value = "${BLOG.HOT_TAG_COUNT}")
    private Integer BLOG_HOT_TAG_COUNT;
    @Value(value = "${BLOG.NEW_COUNT}")
    private Integer BLOG_NEW_COUNT;
    @Value(value = "${BLOG.FIRST_COUNT}")
    private Integer BLOG_FIRST_COUNT;
    @Value(value = "${BLOG.SECOND_COUNT}")
    private Integer BLOG_SECOND_COUNT;
    @Value(value = "${BLOG.THIRD_COUNT}")
    private Integer BLOG_THIRD_COUNT;
    @Value(value = "${BLOG.FOURTH_COUNT}")
    private Integer BLOG_FOURTH_COUNT;

    @ApiOperation(value = "通过推荐等级获取博客列表", notes = "通过推荐等级获取博客列表")
    @GetMapping("/getBlogByLevel")
    public String getBlogByLevel(HttpServletRequest request,
                                 @ApiParam(name = "level", value = "推荐等级", required = false) @RequestParam(name = "level", required = false, defaultValue = "0") Integer level,
                                 @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage) {

        //从Redis中获取内容
        String jsonResult = stringRedisTemplate.opsForValue().get("BOLG_LEVEL:" + level);

        //判断redis中是否有文章
        if (StringUtils.isNotEmpty(jsonResult)) {
            List list = JsonHelper.jsonArrayToArrayList(jsonResult);
            IPage pageList = new Page();
            pageList.setRecords(list);
            return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
        }
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        switch (level) {
            case 0: {
                page.setSize(BLOG_NEW_COUNT);
            }
            break;
            case 1: {
                page.setSize(BLOG_FIRST_COUNT);
            }
            break;
            case 2: {
                page.setSize(BLOG_SECOND_COUNT);
            }
            break;
            case 3: {
                page.setSize(BLOG_THIRD_COUNT);
            }
            break;
            case 4: {
                page.setSize(BLOG_FOURTH_COUNT);
            }
            break;
        }
        IPage<Blog> pageList = blogService.getBlogPageByLevel(page, level);
        List<Blog> list = pageList.getRecords();

        // 一级推荐或者二级推荐没有内容时，自动把top5填充至一级推荐和二级推荐中
        if ((level == SystemConfiguration.ONE || level == SystemConfiguration.TWO) && list.size() == 0) {
            QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
            Page<Blog> hotPage = new Page<>();
            page.setCurrent(1);
            page.setSize(BLOG_HOT_COUNT);
            queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
            queryWrapper.eq(SQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
            queryWrapper.orderByDesc(SQLConfiguration.CLICK_COUNT);
            queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));
            IPage<Blog> hotPageList = blogService.page(page, queryWrapper);
            List<Blog> hotBlogList = hotPageList.getRecords();
            List<Blog> secondBlogList = new ArrayList<>();
            List<Blog> firstBlogList = new ArrayList<>();
            for (int a = 0; a < hotBlogList.size(); a++) {
                // 当推荐大于两个的时候
                if ((hotBlogList.size() - firstBlogList.size()) > BLOG_SECOND_COUNT) {
                    firstBlogList.add(hotBlogList.get(a));
                } else {
                    secondBlogList.add(hotBlogList.get(a));
                }
            }

            firstBlogList = setBlog(firstBlogList);
            secondBlogList = setBlog(secondBlogList);

            //将从数据库查询的数据缓存到redis中，设置1小时后过期
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + SystemConfiguration.ONE, JsonHelper.objectToJson(firstBlogList).toString(), 1, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + SystemConfiguration.TWO, JsonHelper.objectToJson(secondBlogList).toString(), 1, TimeUnit.HOURS);

            switch (level) {
                case SystemConfiguration.ONE: {
                    pageList.setRecords(firstBlogList);
                }
                ;
                break;
                case SystemConfiguration.TWO: {
                    pageList.setRecords(secondBlogList);
                }
                ;
                break;
            }
            return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
        }

        list = setBlog(list);

        pageList.setRecords(list);

        //将从数据库查询的数据缓存到redis中
        String key = SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + level;
        stringRedisTemplate.opsForValue().set(SystemConfiguration.BLOG_LEVEL + SystemConfiguration.REDIS_SEGMENTATION + level, JsonHelper.objectToJson(list).toString(), 10, TimeUnit.SECONDS);

        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "获取首页排行博客", notes = "获取首页排行博客")
    @GetMapping("/getHotBlog")
    public String getHotBlog(HttpServletRequest request) {

        log.info("获取首页排行博客");

        //从Redis中获取内容
        String jsonResult = stringRedisTemplate.opsForValue().get(SystemConfiguration.HOT_BLOG);

        //判断redis中是否有文章
        if (StringUtils.isNotEmpty(jsonResult)) {
            List list = JsonHelper.jsonArrayToArrayList(jsonResult);
            IPage pageList = new Page();
            pageList.setRecords(list);
            log.info("从Redis中返回最新博客");
            return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
        }

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        Page<Blog> page = new Page<>();
        page.setCurrent(0);
        page.setSize(BLOG_HOT_COUNT);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(SQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        queryWrapper.orderByDesc(SQLConfiguration.CLICK_COUNT);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));

        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();

        list = setBlog(list);

        //将从热门博客缓存到redis中
        stringRedisTemplate.opsForValue().set(SystemConfiguration.HOT_BLOG, JsonHelper.objectToJson(list).toString());

        pageList.setRecords(list);
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "获取首页最新的博客", notes = "获取首页最新的博客")
    @GetMapping("/getNewBlog")
    public String getNewBlog(HttpServletRequest request,
                             @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                             @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        log.info("获取首页最新的博客");

        // 只缓存第一页的内容
        if (currentPage == 1L) {
            //从Redis中获取内容
            String jsonResult = stringRedisTemplate.opsForValue().get(SystemConfiguration.NEW_BLOG);

            //判断redis中是否有文章
            if (StringUtils.isNotEmpty(jsonResult)) {
                log.info("从Redis中返回最新博客");
                List list = JsonHelper.jsonArrayToArrayList(jsonResult);
                IPage pageList = new Page();
                pageList.setRecords(list);
                return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
            }
        }
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(BLOG_NEW_COUNT);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));

        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();

        if (list.size() <= 0) {
            return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
        }

        list = setBlog(list);

        //将从最新博客缓存到redis中
        if (currentPage == 1L) {
            stringRedisTemplate.opsForValue().set(SystemConfiguration.NEW_BLOG, JsonHelper.objectToJson(list).toString());
            log.info("将数据缓存至Redis中");
        }
        pageList.setRecords(list);
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "按时间戳获取博客", notes = "按时间戳获取博客")
    @GetMapping("/getBlogByTime")
    public String getBlogByTime(HttpServletRequest request,
                                @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(BasicSQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));

        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();

        list = setBlog(list);

        log.info("按时间戳获取博客");
        pageList.setRecords(list);
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "获取最热标签", notes = "获取最热标签")
    @GetMapping("/getHotTag")
    public String getHotTag(HttpServletRequest request) {

        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        Page<Tag> page = new Page<>();
        page.setCurrent(1);
        page.setSize(BLOG_HOT_TAG_COUNT);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        queryWrapper.orderByDesc(SQLConfiguration.CLICK_COUNT);
        IPage<Tag> pageList = tagService.page(page, queryWrapper);
        log.info("获取最热标签");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "获取友情链接", notes = "获取友情链接")
    @GetMapping("/getLink")
    public String getLink(HttpServletRequest request,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<Link> queryWrapper = new QueryWrapper<>();
        Page<Link> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        IPage<Link> pageList = linkService.page(page, queryWrapper);
        log.info("获取友情链接");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "增加友情链接点击数", notes = "增加友情链接点击数")
    @GetMapping("/addLinkCount")
    public String addLinkCount(HttpServletRequest request,
                               @ApiParam(name = "uid", value = "友情链接UID", required = false) @RequestParam(name = "uid", required = false) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        Link link = linkService.getById(uid);
        if (link != null) {

            //增加记录（可以考虑使用AOP）
            webVisitService.addWebVisit(null, request, Behavior.FRIENDSHIP_LINK.getBehavior(), uid, null);

            int count = link.getClickCount() + 1;
            link.setClickCount(count);
            link.updateById();
        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }


    @ApiOperation(value = "获取网站配置", notes = "获取友情链接")
    @GetMapping("/getWebConfig")
    public String getWebConfig(HttpServletRequest request) {

        QueryWrapper<WebConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        WebConfig webConfig = webConfigService.getOne(queryWrapper);

        if (StringUtils.isNotEmpty(webConfig.getLogo())) {
            String pictureList = this.pictureFeignClient.getPicture(webConfig.getLogo(), SystemConfiguration.FILE_SEGMENTATION);
            webConfig.setPhotoList(WebUtils.getPicture(pictureList));
        }

        //获取支付宝收款二维码
        if (webConfig != null && StringUtils.isNotEmpty(webConfig.getAliPay())) {
            String pictureList = this.pictureFeignClient.getPicture(webConfig.getAliPay(), SystemConfiguration.FILE_SEGMENTATION);
            if (WebUtils.getPicture(pictureList).size() > 0) {
                webConfig.setAliPayPhoto(WebUtils.getPicture(pictureList).get(0));
            }

        }
        //获取微信收款二维码
        if (webConfig != null && StringUtils.isNotEmpty(webConfig.getWeixinPay())) {
            String pictureList = this.pictureFeignClient.getPicture(webConfig.getWeixinPay(), SystemConfiguration.FILE_SEGMENTATION);
            if (WebUtils.getPicture(pictureList).size() > 0) {
                webConfig.setWeixinPayPhoto(WebUtils.getPicture(pictureList).get(0));
            }

        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, webConfig);
    }

    @ApiOperation(value = "记录访问页面", notes = "记录访问页面")
    @GetMapping("/recorderVisitPage")
    public String recorderVisitPage(HttpServletRequest request,
                                    @ApiParam(name = "pageName", value = "页面名称", required = false) @RequestParam(name = "pageName", required = true) String pageName) {

        if (StringUtils.isEmpty(pageName)) {
            return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.PARAM_INCORRECT);
        }

        webVisitService.addWebVisit(null, request, Behavior.VISIT_PAGE.getBehavior(), null, pageName);

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
    }


    /**
     * 设置博客的分类标签和内容
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
        String pictureList = null;

        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SystemConfiguration.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = WebUtils.getPictureMap(pictureList);
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

        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConfiguration.UID).toString(), item.get(SQLConfiguration.URL).toString());
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

            //获取图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SystemConfiguration.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();

                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }
        }
        return list;
    }
}

