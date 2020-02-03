package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Original;
import org.fantasizer.common.enums.Publish;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.exception.ThrowableHelper;
import org.fantasizer.common.helper.DateUtils;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.helper.WebUtils;
import org.fantasizer.common.validator.group.Delete;
import org.fantasizer.common.validator.group.FetchList;
import org.fantasizer.common.validator.group.Insert;
import org.fantasizer.common.validator.group.Update;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.entity.Blog;
import org.fantasizer.theblog.xo.entity.BlogCatalog;
import org.fantasizer.theblog.xo.entity.Tag;
import org.fantasizer.theblog.xo.service.*;
import org.fantasizer.theblog.xo.vo.BlogVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 博客表 RestApi
 * </p>
 *
 * @author xzx19950624@qq.com
 * @since 2018-09-08
 */

@RestController
@RequestMapping("/blog")
@Api(value = "博客RestApi", tags = {"BlogRestApi"})
public class BlogRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    BlogService blogService;
    @Autowired
    TagService tagService;
    @Autowired
    BlogCatalogService blogCatalogService;
    @Autowired
    AdministratorService administratorService;
    @Autowired
    private PictureFeignClient pictureFeignClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value(value = "${data.image.url}")
    private String IMG_HOST;
    @Value(value = "${PROJECT_NAME}")
    private String PROJECT_NAME;
    @Value(value = "${BLOG.FIRST_COUNT}")
    private Integer BLOG_FIRST_COUNT;
    @Value(value = "${BLOG.SECOND_COUNT}")
    private Integer BLOG_SECOND_COUNT;
    @Value(value = "${BLOG.THIRD_COUNT}")
    private Integer BLOG_THIRD_COUNT;
    @Value(value = "${BLOG.FOURTH_COUNT}")
    private Integer BLOG_FOURTH_COUNT;
    @Value(value = "${spring.data.solr.core}")
    private String collection;
    @Autowired
    private BlogSearchService blogSearchService;

    @ApiOperation(value = "获取博客列表", notes = "获取博客列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody BlogVO blogVO, BindingResult result) {

        ThrowableHelper.checkParamArgument(result);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(blogVO.getKeyword()) && !StringUtils.isEmpty(blogVO.getKeyword().trim())) {
            queryWrapper.like(SQLConfiguration.TITLE, blogVO.getKeyword().trim());
        }
        if (!StringUtils.isEmpty(blogVO.getTagUid())) {
            queryWrapper.like(SQLConfiguration.TAG_UID, blogVO.getTagUid());
        }
        if (!StringUtils.isEmpty(blogVO.getBlogSortUid())) {
            queryWrapper.like(SQLConfiguration.BLOG_SORT_UID, blogVO.getBlogSortUid());
        }
        if (!StringUtils.isEmpty(blogVO.getLevelKeyword())) {
            queryWrapper.eq(SQLConfiguration.LEVEL, blogVO.getLevelKeyword());
        }
        if (!StringUtils.isEmpty(blogVO.getIsPublish())) {
            queryWrapper.eq(SQLConfiguration.IS_PUBLISH, blogVO.getIsPublish());
        }
        if (!StringUtils.isEmpty(blogVO.getIsOriginal())) {
            queryWrapper.eq(SQLConfiguration.IS_ORIGINAL, blogVO.getIsOriginal());
        }

        //分页
        Page<Blog> page = new Page<>();
        page.setCurrent(blogVO.getCurrentPage());
        page.setSize(blogVO.getPageSize());

        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);

        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);

        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();

        if (list.size() == 0) {
            return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
        }

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
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SystemConfiguration.FILE_SEGMENTATION);
                for (String itemTagUid : tagUidsTemp) {
                    tagUids.add(itemTagUid);
                }
            }
        });
        String pictureList = null;

        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SystemConfiguration.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = WebUtils.getPictureMap(pictureList);
        Collection<BlogCatalog> sortList = blogCatalogService.listByIds(sortUids);
        Collection<Tag> tagList = tagService.listByIds(tagUids);

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

        pageList.setRecords(list);
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加博客")
    @ApiOperation(value = "增加博客", notes = "增加博客", response = String.class)
    @PostMapping("/add")
    public String add(HttpServletRequest request, @Validated({Insert.class}) @RequestBody BlogVO blogVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.LEVEL, blogVO.getLevel());
        Integer count = blogService.count(queryWrapper);

        String addVerdictResult = addVerdict(count, blogVO.getLevel());

        // 判断是否能够添加推荐
        if (StringUtils.isNotBlank(addVerdictResult)) {
            return addVerdictResult;
        }

        Blog blog = new Blog();

        //如果是原创，作者为用户的昵称
        if (Original.ORIGINAL.equals(blogVO.getIsOriginal())) {
            Administrator admin = administratorService.getById(request.getAttribute(SystemConfiguration.ADMIN_UID).toString());
            if (admin != null) {
                blog.setAuthor(admin.getNickName());
                blog.setAdminUid(admin.getUid());
            }
            blog.setArticlesPart(PROJECT_NAME);
        } else {
            blog.setAuthor(blogVO.getAuthor());
            blog.setArticlesPart(blogVO.getArticlesPart());
        }

        blog.setTitle(blogVO.getTitle());
        blog.setSummary(blogVO.getSummary());
        blog.setContent(blogVO.getContent());
        blog.setTagUid(blogVO.getTagUid());
        blog.setBlogSortUid(blogVO.getBlogSortUid());
        blog.setFileUid(blogVO.getFileUid());
        blog.setLevel(blogVO.getLevel());
        blog.setIsOriginal(blogVO.getIsOriginal());
        blog.setIsPublish(blogVO.getIsPublish());
        blog.setStatus(Status.ENABLED);
        Boolean isSave = blogService.save(blog);

        //保存成功后，需要发送消息到solr 和 redis
        updateSolrAndRedis(isSave, blog);

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
    }

    @OperationLogger(value = "编辑博客")
    @ApiOperation(value = "编辑博客", notes = "编辑博客", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request, @Validated({Update.class}) @RequestBody BlogVO blogVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Blog blog = blogService.getById(blogVO.getUid());
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.LEVEL, blogVO.getLevel());
        Integer count = blogService.count(queryWrapper);
        if (blog != null) {
            //传递过来的和数据库中的不同，代表用户已经修改过等级了，那么需要将count数加1
            if (blog.getLevel().equals(blogVO.getLevel())) {
                count += 1;
            }
        }
        String addVerdictResult = addVerdict(count, blogVO.getLevel());
        //添加的时候进行判断
        if (StringUtils.isNotBlank(addVerdictResult)) {
            return addVerdictResult;
        }

        //如果是原创，作者为用户的昵称
        Administrator admin = administratorService.getById(request.getAttribute(SystemConfiguration.ADMIN_UID).toString());
        blog.setAdminUid(admin.getUid());
        if (Original.ORIGINAL.equals(blogVO.getIsOriginal())) {
            blog.setAuthor(admin.getNickName());
            blog.setArticlesPart(PROJECT_NAME);
        } else {
            blog.setAuthor(blogVO.getAuthor());
            blog.setArticlesPart(blogVO.getArticlesPart());
        }

        blog.setTitle(blogVO.getTitle());
        blog.setSummary(blogVO.getSummary());
        blog.setContent(blogVO.getContent());
        blog.setTagUid(blogVO.getTagUid());
        blog.setBlogSortUid(blogVO.getBlogSortUid());
        blog.setFileUid(blogVO.getFileUid());
        blog.setLevel(blogVO.getLevel());
        blog.setIsOriginal(blogVO.getIsOriginal());
        blog.setIsPublish(blogVO.getIsPublish());
        blog.setStatus(Status.ENABLED);

        Boolean isSave = blog.updateById();

        //保存成功后，需要发送消息到solr 和 redis
        updateSolrAndRedis(isSave, blog);

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }

    @OperationLogger(value = "删除博客")
    @ApiOperation(value = "删除博客", notes = "删除博客", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request, @Validated({Delete.class}) @RequestBody BlogVO blogVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Blog blog = blogService.getById(blogVO.getUid());
        blog.setStatus(Status.DELETED);
        Boolean save = blog.updateById();

        //保存成功后，需要发送消息到solr 和 redis
        if (save) {
            Map<String, Object> map = new HashMap<>();
            map.put(SystemConfiguration.COMMAND, SystemConfiguration.DELETE);
            map.put(SystemConfiguration.BLOG_UID, blog.getUid());
            map.put(SystemConfiguration.LEVEL, blog.getLevel());
            String dateTime = DateUtils.dateTimeToStr(blog.getCreateTime());
            System.out.println(dateTime);
            map.put(SystemConfiguration.CREATE_TIME, dateTime);
            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SystemConfiguration.EXCHANGE_DIRECT, SystemConfiguration.MOGU_BLOG, map);

            //删除solr索引
            blogSearchService.deleteIndex(collection, blog.getUid());
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }

    @OperationLogger(value = "删除选中博客")
    @ApiOperation(value = "删除选中博客", notes = "删除选中博客", response = String.class)
    @PostMapping("/deleteBatch")
    public String deleteBatch(HttpServletRequest request, @RequestBody List<BlogVO> blogVoList) {

        if (blogVoList.size() <= 0) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        List<String> uids = new ArrayList<>();
        blogVoList.forEach(item -> {
            uids.add(item.getUid());
        });
        Collection<Blog> blogList = blogService.listByIds(uids);

        blogList.forEach(item -> {
            item.setStatus(Status.DELETED);
        });

        Boolean save = blogService.updateBatchById(blogList);

        //保存成功后，需要发送消息到solr 和 redis
        if (save) {

            Map<String, Object> map = new HashMap<>();
            map.put(SystemConfiguration.COMMAND, SystemConfiguration.DELETE_BATCH);

            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SystemConfiguration.EXCHANGE_DIRECT, SystemConfiguration.MOGU_BLOG, map);

            //删除solr索引
            blogSearchService.deleteBatchIndex(collection, uids);
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }


    /**
     * 添加时校验
     *
     * @param count
     * @param level
     * @return
     */
    private String addVerdict(Integer count, Integer level) {
        //添加的时候进行判断
        switch (level) {
            case 1: {
                if (count > BLOG_FIRST_COUNT) {
                    return ResultUtil.result(SystemConfiguration.ERROR, "一级推荐不能超过" + BLOG_FIRST_COUNT + "个");
                }
            }
            break;

            case 2: {
                if (count > BLOG_SECOND_COUNT) {
                    return ResultUtil.result(SystemConfiguration.ERROR, "二级推荐不能超过" + BLOG_SECOND_COUNT + "个");
                }
            }
            break;

            case 3: {
                if (count > BLOG_THIRD_COUNT) {
                    return ResultUtil.result(SystemConfiguration.ERROR, "三级推荐不能超过" + BLOG_THIRD_COUNT + "个");
                }
            }
            break;

            case 4: {
                if (count > BLOG_FOURTH_COUNT) {
                    return ResultUtil.result(SystemConfiguration.ERROR, "四级推荐不能超过" + BLOG_FOURTH_COUNT + "个");
                }
            }
            break;
            default: {

            }
        }
        return null;
    }

    /**
     * 设置图片
     *
     * @param blog
     */
    private void setPhoto(Blog blog) {
        if (StringUtils.isNotEmpty(blog.getFileUid())) {
            String pictureList = this.pictureFeignClient.getPicture(blog.getFileUid(), SystemConfiguration.FILE_SEGMENTATION);
            List<String> picList = WebUtils.getPicture(pictureList);
            blog.setPhotoList(picList);
        }
    }

    /**
     * 保存成功后，需要发送消息到solr 和 redis
     *
     * @param isSave
     * @param blog
     */
    private void updateSolrAndRedis(Boolean isSave, Blog blog) {
        // 保存操作，并且文章已设置发布
        if (isSave && Publish.PUBLISH.equals(blog.getIsPublish())) {
            Map<String, Object> map = new HashMap<>();
            map.put(SystemConfiguration.COMMAND, SystemConfiguration.ADD);
            map.put(SystemConfiguration.BLOG_UID, blog.getUid());
            map.put(SystemConfiguration.LEVEL, blog.getLevel());
            map.put(SystemConfiguration.CREATE_TIME, blog.getCreateTime());

            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SystemConfiguration.EXCHANGE_DIRECT, SystemConfiguration.MOGU_BLOG, map);

            //设置图片
            setPhoto(blog);

            //增加solr索引
            blogSearchService.addIndex(collection, blog);
        } else if (Publish.UNPUBLISH.equals(blog.getIsPublish())) {

            //这是需要做的是，是删除redis中的该条博客数据
            Map<String, Object> map = new HashMap<>();
            map.put(SystemConfiguration.COMMAND, SystemConfiguration.EDIT);
            map.put(SystemConfiguration.BLOG_UID, blog.getUid());
            map.put(SystemConfiguration.LEVEL, blog.getLevel());
            map.put(SystemConfiguration.CREATE_TIME, blog.getCreateTime());

            //发送到RabbitMq
            rabbitTemplate.convertAndSend(SystemConfiguration.EXCHANGE_DIRECT, SystemConfiguration.MOGU_BLOG, map);

            //当设置下架状态时，删除博客索引
            blogSearchService.deleteIndex(collection, blog.getUid());
        }
    }
}