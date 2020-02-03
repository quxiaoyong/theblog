package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
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
import org.fantasizer.theblog.xo.entity.Tag;
import org.fantasizer.theblog.xo.service.BlogService;
import org.fantasizer.theblog.xo.service.TagService;
import org.fantasizer.theblog.xo.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(value = "标签RestApi", tags = {"TagRestApi"})
@RestController
@RequestMapping("/tag")
public class TagRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    TagService tagService;
    @Autowired
    BlogService blogService;

    @ApiOperation(value = "获取标签列表", notes = "获取标签列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody TagVO tagVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(tagVO.getKeyword()) && !StringUtils.isEmpty(tagVO.getKeyword())) {
            queryWrapper.like(SQLConfiguration.CONTENT, tagVO.getKeyword().trim());
        }

        Page<Tag> page = new Page<>();
        page.setCurrent(tagVO.getCurrentPage());
        page.setSize(tagVO.getPageSize());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        IPage<Tag> pageList = tagService.page(page, queryWrapper);
        log.info("获取标签列表");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加标签")
    @ApiOperation(value = "增加标签", notes = "增加标签", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody TagVO tagVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.CONTENT, tagVO.getContent());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        Tag tempTag = tagService.getOne(queryWrapper);
        if (tempTag != null) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.ENTITY_EXIST);
        }
        Tag tag = new Tag();
        tag.setContent(tagVO.getContent());
        tag.setClickCount(0);
        tag.setStatus(Status.ENABLED);
        tag.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
    }

    @OperationLogger(value = "编辑标签")
    @ApiOperation(value = "编辑标签", notes = "编辑标签", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody TagVO tagVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Tag tag = tagService.getById(tagVO.getUid());

        if (tag != null && !tag.getContent().equals(tagVO.getContent())) {
            QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SQLConfiguration.CONTENT, tagVO.getContent());
            queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
            Tag tempTag = tagService.getOne(queryWrapper);
            if (tempTag != null) {
                return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.ENTITY_EXIST);
            }
        }

        tag.setContent(tagVO.getContent());
        tag.setStatus(Status.ENABLED);
        tag.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }

    @OperationLogger(value = "批量删除标签")
    @ApiOperation(value = "批量删除标签", notes = "批量删除标签", response = String.class)
    @PostMapping("/deleteBatch")
    public String delete(@Validated({Delete.class}) @RequestBody List<TagVO> tagVoList, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        if (tagVoList.size() <= 0) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        List<String> uids = new ArrayList<>();
        tagVoList.forEach(item -> {
            uids.add(item.getUid());
        });
        Collection<Tag> tagList = tagService.listByIds(uids);

        tagList.forEach(item -> {
            item.setStatus(Status.DELETED);
        });

        Boolean save = tagService.updateBatchById(tagList);

        if (save) {
            return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.DELETE_FAIL);
        }
    }

    @OperationLogger(value = "置顶标签")
    @ApiOperation(value = "置顶标签", notes = "置顶标签", response = String.class)
    @PostMapping("/stick")
    public String stick(@Validated({Delete.class}) @RequestBody TagVO tagVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Tag tag = tagService.getById(tagVO.getUid());

        //查找出最大的那一个
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        Page<Tag> page = new Page<>();
        page.setCurrent(0);
        page.setSize(1);
        IPage<Tag> pageList = tagService.page(page, queryWrapper);
        List<Tag> list = pageList.getRecords();
        Tag maxTag = list.get(0);

        if (StringUtils.isEmpty(maxTag.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        if (maxTag.getUid().equals(tag.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.OPERATION_FAIL);
        }

        Integer sortCount = maxTag.getSort() + 1;

        tag.setSort(sortCount);

        tag.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_FAIL);
    }

    @OperationLogger(value = "通过点击量排序标签")
    @ApiOperation(value = "通过点击量排序标签", notes = "通过点击量排序标签", response = String.class)
    @PostMapping("/tagSortByClickCount")
    public String tagSortByClickCount() {

        QueryWrapper<Tag> queryWrapper = new QueryWrapper();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        // 按点击从高到低排序
        queryWrapper.orderByDesc(SQLConfiguration.CLICK_COUNT);
        List<Tag> tagList = tagService.list(queryWrapper);
        // 设置初始化最大的sort值
        Integer maxSort = tagList.size();
        for (Tag item : tagList) {
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
    @OperationLogger(value = "通过引用量排序标签")
    @ApiOperation(value = "通过引用量排序标签", notes = "通过引用量排序标签", response = String.class)
    @PostMapping("/tagSortByCite")
    public String tagSortByCite() {

        // 定义Map   key：tagUid,  value: 引用量
        Map<String, Integer> map = new HashMap<>();

        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        List<Tag> tagList = tagService.list(tagQueryWrapper);
        // 初始化所有标签的引用量
        tagList.forEach(item -> {
            map.put(item.getUid(), 0);
        });

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(SQLConfiguration.IS_PUBLISH, Publish.PUBLISH);
        // 过滤content字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals(SQLConfiguration.CONTENT));
        List<Blog> blogList = blogService.list(queryWrapper);

        blogList.forEach(item -> {
            String tagUids = item.getTagUid();
            List<String> tagUidList = StringUtils.changeStringToString(tagUids, SystemConfiguration.FILE_SEGMENTATION);
            for (String tagUid : tagUidList) {
                if (map.get(tagUid) != null) {
                    Integer count = map.get(tagUid) + 1;
                    map.put(tagUid, count);
                } else {
                    map.put(tagUid, 0);
                }
            }
        });

        tagList.forEach(item -> {
            item.setSort(map.get(item.getUid()));
            item.updateById();
        });

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_SUCCESS);
    }
}

