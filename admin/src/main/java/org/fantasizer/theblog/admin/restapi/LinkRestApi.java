package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.exception.ThrowableHelper;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.validator.group.Delete;
import org.fantasizer.theblog.common.validator.group.FetchList;
import org.fantasizer.theblog.common.validator.group.Insert;
import org.fantasizer.theblog.common.validator.group.Update;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Link;
import org.fantasizer.theblog.xo.service.LinkService;
import org.fantasizer.theblog.xo.vo.LinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "友链RestApi", tags = {"LinkRestApi"})
@RequestMapping("/link")
public class LinkRestApi {
    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    LinkService linkService;

    @ApiOperation(value = "获取友链列表", notes = "获取友链列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody LinkVO linkVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<Link> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(linkVO.getKeyword()) && !StringUtils.isEmpty(linkVO.getKeyword().trim())) {
            queryWrapper.like(SQLConfiguration.TITLE, linkVO.getKeyword().trim());
        }

        Page<Link> page = new Page<>();
        page.setCurrent(linkVO.getCurrentPage());
        page.setSize(linkVO.getPageSize());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        IPage<Link> pageList = linkService.page(page, queryWrapper);
        log.info("获取友链列表");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加友链")
    @ApiOperation(value = "增加友链", notes = "增加友链", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody LinkVO linkVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Link link = new Link();
        link.setTitle(linkVO.getTitle());
        link.setSummary(linkVO.getSummary());
        link.setUrl(linkVO.getUrl());
        link.setClickCount(0);
        link.setStatus(Status.ENABLED);
        link.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
    }

    @OperationLogger(value = "编辑友链")
    @ApiOperation(value = "编辑友链", notes = "编辑友链", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody LinkVO linkVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Link link = linkService.getById(linkVO.getUid());
        link.setTitle(linkVO.getTitle());
        link.setSummary(linkVO.getSummary());
        link.setUrl(linkVO.getUrl());
        link.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }

    @OperationLogger(value = "删除友链")
    @ApiOperation(value = "删除友链", notes = "删除友链", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody LinkVO linkVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Link tag = linkService.getById(linkVO.getUid());
        tag.setStatus(Status.DELETED);
        tag.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }

    @ApiOperation(value = "置顶友链", notes = "置顶友链", response = String.class)
    @PostMapping("/stick")
    public String stick(@Validated({Delete.class}) @RequestBody LinkVO linkVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Link link = linkService.getById(linkVO.getUid());

        //查找出最大的那一个
        QueryWrapper<Link> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        Page<Link> page = new Page<>();
        page.setCurrent(0);
        page.setSize(1);
        IPage<Link> pageList = linkService.page(page, queryWrapper);
        List<Link> list = pageList.getRecords();
        Link maxSort = list.get(0);
        if (StringUtils.isEmpty(maxSort.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        if (maxSort.getUid().equals(link.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.OPERATION_FAIL);
        }

        Integer sortCount = maxSort.getSort() + 1;

        link.setSort(sortCount);

        link.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_SUCCESS);
    }
}