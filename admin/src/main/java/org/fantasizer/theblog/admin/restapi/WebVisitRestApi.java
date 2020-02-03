package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fantasizer.common.enums.Behavior;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.exception.ThrowableHelper;
import org.fantasizer.common.helper.DateUtils;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.validator.group.FetchList;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.*;
import org.fantasizer.theblog.xo.service.*;
import org.fantasizer.theblog.xo.vo.WebVisitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.Collection;
@Api(value = "用户访问RestApi", tags = {"用户访问RestApi"})
@RestController
@RequestMapping("/webConfig")
public class WebVisitRestApi {

    @Autowired
    WebVisitService webVisitService;

    @Autowired
    TagService tagService;

    @Autowired
    BlogCatalogService blogCatalogService;

    @Autowired
    BlogService blogService;

    @Autowired
    LinkService linkService;


    @ApiOperation(value = "获取用户访问列表", notes = "获取用户访问列表")
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody WebVisitVO webVisitVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<WebVisit> queryWrapper = new QueryWrapper<>();

        // 得到所有的枚举对象
        Behavior[] arr = Behavior.values();

        // 设置关键字查询
        if (!StringUtils.isEmpty(webVisitVO.getKeyword()) && !StringUtils.isEmpty(webVisitVO.getKeyword().trim())) {

            String behavior = "";
            for (int a = 0; a < arr.length; a++) {
                // 设置行为名称
                if (arr[a].getContent().equals(webVisitVO.getKeyword().trim())) {
                    behavior = arr[a].getBehavior();
                }
            }

            queryWrapper.like(SQLConfiguration.IP, webVisitVO.getKeyword().trim()).or().eq(SQLConfiguration.BEHAVIOR, behavior);
        }

        // 设置起始时间段
        if (!StringUtils.isEmpty(webVisitVO.getStartTime())) {
            String[] time = webVisitVO.getStartTime().split(SystemConfiguration.FILE_SEGMENTATION);
            if (time.length < 2) {
                return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
            }
            queryWrapper.between(SQLConfiguration.CREATE_TIME, DateUtils.str2Date(time[0]), DateUtils.str2Date(time[1]));
        }

        Page<WebVisit> page = new Page<>();
        page.setCurrent(webVisitVO.getCurrentPage());
        page.setSize(webVisitVO.getPageSize());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        IPage<WebVisit> pageList = webVisitService.page(page, queryWrapper);

        List<WebVisit> list = pageList.getRecords();
        List<String> blogUids = new ArrayList<>();
        List<String> tagUids = new ArrayList<>();
        List<String> sortUids = new ArrayList<>();
        List<String> linkUids = new ArrayList<>();

        list.forEach(item -> {
            if (item.getBehavior().equals(Behavior.BLOG_CONTNET.getBehavior())) {
                blogUids.add(item.getModuleUid());
            } else if (item.getBehavior().equals(Behavior.BLOG_SORT.getBehavior())) {
                sortUids.add(item.getModuleUid());
            } else if (item.getBehavior().equals(Behavior.BLOG_TAG.getBehavior())) {
                tagUids.add(item.getModuleUid());
            } else if (item.getBehavior().equals(Behavior.FRIENDSHIP_LINK.getBehavior())) {
                linkUids.add(item.getModuleUid());
            }
        });
        Collection<Blog> blogList = new ArrayList<>();
        Collection<Tag> tagList = new ArrayList<>();
        Collection<BlogCatalog> sortList = new ArrayList<>();
        Collection<Link> linkList = new ArrayList<>();

        if (blogUids.size() > 0) {
            blogList = blogService.listByIds(blogUids);
        }

        if (tagUids.size() > 0) {
            tagList = tagService.listByIds(tagUids);
        }

        if (sortUids.size() > 0) {
            sortList = blogCatalogService.listByIds(sortUids);
        }

        if (linkUids.size() > 0) {
            linkList = linkService.listByIds(linkUids);
        }

        Map<String, String> contentMap = new HashMap<>();
        blogList.forEach(item -> {
            contentMap.put(item.getUid(), item.getTitle());
        });

        tagList.forEach(item -> {
            contentMap.put(item.getUid(), item.getContent());
        });

        sortList.forEach(item -> {
            contentMap.put(item.getUid(), item.getContent());
        });

        linkList.forEach(item -> {
            contentMap.put(item.getUid(), item.getTitle());
        });

        list.forEach(item -> {

            for (int a = 0; a < arr.length; a++) {
                // 设置行为名称
                if (arr[a].getBehavior().equals(item.getBehavior())) {
                    item.setBehaviorContent(arr[a].getContent());
                    break;
                }
            }

            if (item.getBehavior().equals(Behavior.BLOG_CONTNET.getBehavior()) ||
                    item.getBehavior().equals(Behavior.BLOG_SORT.getBehavior()) ||
                    item.getBehavior().equals(Behavior.BLOG_TAG.getBehavior()) ||
                    item.getBehavior().equals(Behavior.FRIENDSHIP_LINK.getBehavior())) {

                //从map中获取到对应的名称
                if (StringUtils.isNotEmpty(item.getModuleUid())) {
                    item.setContent(contentMap.get(item.getModuleUid()));
                }

            } else {
                item.setContent(item.getOtherData());
            }
        });

        pageList.setRecords(list);

        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }


}

