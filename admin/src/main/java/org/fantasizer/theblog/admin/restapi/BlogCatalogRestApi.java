package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Publish;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.exception.ThrowableHelper;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.validator.group.Delete;
import org.fantasizer.common.validator.group.FetchList;
import org.fantasizer.common.validator.group.Insert;
import org.fantasizer.common.validator.group.Update;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Blog;
import org.fantasizer.theblog.xo.entity.BlogCatalog;
import org.fantasizer.theblog.xo.service.BlogCatalogService;
import org.fantasizer.theblog.xo.service.BlogService;
import org.fantasizer.theblog.xo.vo.BlogCatalogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/blogSort")
public class BlogCatalogRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    BlogCatalogService blogCatalogService;
    @Autowired
    BlogService blogService;

    @ApiOperation(value = "获取博客分类列表", notes = "获取博客分类列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody BlogCatalogVO blogSortVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<BlogCatalog> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(blogSortVO.getKeyword()) && !StringUtils.isEmpty(blogSortVO.getKeyword().trim())) {
            queryWrapper.like(SQLConfiguration.SORT_NAME, blogSortVO.getKeyword().trim());
        }
        Page<BlogCatalog> page = new Page<>();
        page.setCurrent(blogSortVO.getCurrentPage());
        page.setSize(blogSortVO.getPageSize());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        IPage<BlogCatalog> pageList = blogCatalogService.page(page, queryWrapper);
        log.info("获取博客分类列表");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加博客分类")
    @ApiOperation(value = "增加博客分类", notes = "增加博客分类", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody BlogCatalogVO blogSortVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        // 判断添加的分类是否存在
        QueryWrapper<BlogCatalog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.SORT_NAME, blogSortVO.getCatalogName());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        BlogCatalog tempSort = blogCatalogService.getOne(queryWrapper);
        if (tempSort != null) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.ENTITY_EXIST);
        }

        BlogCatalog blogSort = new BlogCatalog();
        blogSort.setContent(blogSortVO.getContent());
        blogSort.setCatalogName(blogSortVO.getCatalogName());
        blogSort.setStatus(Status.ENABLED);
        blogSort.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
    }

    @OperationLogger(value = "编辑博客分类")
    @ApiOperation(value = "编辑博客分类", notes = "编辑博客分类", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody BlogCatalogVO blogSortVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        BlogCatalog blogSort = blogCatalogService.getById(blogSortVO.getUid());

        /**
         * 判断需要编辑的博客分类是否存在
         */
        if (!blogSort.getCatalogName().equals(blogSortVO.getCatalogName())) {
            QueryWrapper<BlogCatalog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SQLConfiguration.SORT_NAME, blogSortVO.getCatalogName());
            BlogCatalog tempSort = blogCatalogService.getOne(queryWrapper);
            if (tempSort != null) {
                return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.ENTITY_EXIST);
            }
        }

        blogSort.setContent(blogSortVO.getContent());
        blogSort.setCatalogName(blogSortVO.getCatalogName());
        blogSort.setStatus(Status.ENABLED);
        blogSort.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }

    @OperationLogger(value = "批量删除博客分类")
    @ApiOperation(value = "批量删除博客分类", notes = "批量删除博客分类", response = String.class)
    @PostMapping("/deleteBatch")
    public String delete(@Validated({Delete.class}) @RequestBody List<BlogCatalogVO> blogSortVoList, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        if (blogSortVoList.size() <= 0) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        List<String> uids = new ArrayList<>();

        blogSortVoList.forEach(item -> {
            uids.add(item.getUid());
        });

        Collection<BlogCatalog> blogSortList = blogCatalogService.listByIds(uids);

        blogSortList.forEach(item -> {
            item.setStatus(Status.DELETED);
        });

        Boolean save = blogCatalogService.updateBatchById(blogSortList);

        if (save) {
            return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.DELETE_FAIL);
        }
    }

    @ApiOperation(value = "置顶分类", notes = "置顶分类", response = String.class)
    @PostMapping("/stick")
    public String stick(@Validated({Delete.class}) @RequestBody BlogCatalogVO blogSortVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        BlogCatalog blogSort = blogCatalogService.getById(blogSortVO.getUid());

        //查找出最大的那一个
        QueryWrapper<BlogCatalog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        Page<BlogCatalog> page = new Page<>();
        page.setCurrent(0);
        page.setSize(1);
        IPage<BlogCatalog> pageList = blogCatalogService.page(page, queryWrapper);
        List<BlogCatalog> list = pageList.getRecords();
        BlogCatalog maxSort = list.get(0);

        if (StringUtils.isEmpty(maxSort.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        if (maxSort.getUid().equals(blogSort.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.OPERATION_FAIL);
        }

        Integer sortCount = maxSort.getSort() + 1;

        blogSort.setSort(sortCount);

        blogSort.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_SUCCESS);
    }

    @OperationLogger(value = "通过点击量排序博客分类")
    @ApiOperation(value = "通过点击量排序博客分类", notes = "通过点击量排序博客分类", response = String.class)
    @PostMapping("/blogSortByClickCount")
    public String blogSortByClickCount() {

        QueryWrapper<BlogCatalog> queryWrapper = new QueryWrapper();

        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        // 按点击从高到低排序
        queryWrapper.orderByDesc(SQLConfiguration.CLICK_COUNT);

        List<BlogCatalog> blogSortList = blogCatalogService.list(queryWrapper);

        // 设置初始化最大的sort值
        Integer maxSort = blogSortList.size();
        for (BlogCatalog item : blogSortList) {
            item.setSort(item.getClickCount());
            item.updateById();
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_SUCCESS);
    }

    /**
     * 通过引用量排序标签
     * 引用量就是所有的文章中，有多少使用了该标签，如果使用的越多，该标签的引用量越大，那么排名越靠前
     *
     * @return
     */
    @OperationLogger(value = "通过引用量排序博客分类")
    @ApiOperation(value = "通过引用量排序博客分类", notes = "通过引用量排序博客分类", response = String.class)
    @PostMapping("/blogSortByCite")
    public String blogSortByCite() {

        // 定义Map   key：tagUid,  value: 引用量
        Map<String, Integer> map = new HashMap<>();

        QueryWrapper<BlogCatalog> blogSortQueryWrapper = new QueryWrapper<>();
        blogSortQueryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        List<BlogCatalog> blogSortList = blogCatalogService.list(blogSortQueryWrapper);
        // 初始化所有标签的引用量
        blogSortList.forEach(item -> {
            map.put(item.getUid(), 0);
        });

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(SQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        // 过滤content字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));
        List<Blog> blogList = blogService.list(queryWrapper);

        blogList.forEach(item -> {
            String blogSortUid = item.getBlogSortUid();
            if (map.get(blogSortUid) != null) {
                Integer count = map.get(blogSortUid) + 1;
                map.put(blogSortUid, count);
            } else {
                map.put(blogSortUid, 0);
            }
        });

        blogSortList.forEach(item -> {
            item.setSort(map.get(item.getUid()));
            item.updateById();
        });

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_SUCCESS);
    }
}

