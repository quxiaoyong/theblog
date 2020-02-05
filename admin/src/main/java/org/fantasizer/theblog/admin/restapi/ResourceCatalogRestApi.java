package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.helper.WebUtils;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.ResourceCatalog;
import org.fantasizer.theblog.xo.service.ResourceCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "资源分类RestApi", tags = {"ResourceSortRestApi"})
@RestController
@RequestMapping("/resourceSort")
public class ResourceCatalogRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    ResourceCatalogService resourceCatalogService;
    @Autowired
    PictureFeignClient pictureFeignClient;

    @ApiOperation(value = "获取资源分类列表", notes = "获取资源分类列表", response = String.class)
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public String getList(HttpServletRequest request,
                          @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<ResourceCatalog> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(keyword) && !StringUtils.isEmpty(keyword.trim())) {
            queryWrapper.like(SQLConfiguration.SORT_NAME, keyword.trim());
        }

        Page<ResourceCatalog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        IPage<ResourceCatalog> pageList = resourceCatalogService.page(page, queryWrapper);
        List<ResourceCatalog> list = pageList.getRecords();

        final StringBuffer fileUids = new StringBuffer();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUids.append(item.getFileUid() + ",");
            }
        });
        String pictureResult = null;
        Map<String, String> pictureMap = new HashMap<String, String>();

        if (fileUids != null) {
            pictureResult = this.pictureFeignClient.getPicture(fileUids.toString(), ",");
        }
        List<Map<String, Object>> picList = WebUtils.getPictureMap(pictureResult);

        picList.forEach(item -> {
            pictureMap.put(item.get("uid").toString(), item.get("url").toString());
        });

        for (ResourceCatalog item : list) {
            //获取图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), ",");
                List<String> pictureListTemp = new ArrayList<String>();
                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }
        }
        log.info("返回结果");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加资源分类")
    @ApiOperation(value = "增加资源分类", notes = "增加资源分类", response = String.class)
    @PostMapping("/add")
    public String add(HttpServletRequest request,
                      @ApiParam(name = "sortName", value = "资源分类名", required = false) @RequestParam(name = "sortName", required = false) String sortName,
                      @ApiParam(name = "content", value = "分类介绍", required = false) @RequestParam(name = "content", required = false) String content,
                      @ApiParam(name = "fileUid", value = "分类资源UID", required = false) @RequestParam(name = "fileUid", required = false) String fileUid) {

        if (StringUtils.isEmpty(sortName)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        ResourceCatalog resourceSort = new ResourceCatalog();
        resourceSort.setSortName(sortName);
        resourceSort.setContent(content);
        resourceSort.setFileUid(fileUid);
        resourceSort.setStatus(Status.ENABLED);
        resourceSort.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "添加成功");
    }

    @OperationLogger(value = "编辑资源分类")
    @ApiOperation(value = "编辑资源分类", notes = "编辑资源分类", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request,
                       @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid,
                       @ApiParam(name = "sortName", value = "资源分类名", required = false) @RequestParam(name = "sortName", required = false) String sortName,
                       @ApiParam(name = "content", value = "分类介绍", required = false) @RequestParam(name = "content", required = false) String content,
                       @ApiParam(name = "fileUid", value = "分类资源UID", required = false) @RequestParam(name = "fileUid", required = false) String fileUid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }

        ResourceCatalog resourceSort = resourceCatalogService.getById(uid);
        resourceSort.setSortName(sortName);
        resourceSort.setContent(content);
        resourceSort.setFileUid(fileUid);
        resourceSort.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "编辑成功");
    }

    @OperationLogger(value = "删除资源分类")
    @ApiOperation(value = "删除资源分类", notes = "删除资源分类", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        ResourceCatalog resourceSort = resourceCatalogService.getById(uid);
        resourceSort.setStatus(Status.DELETED);
        resourceSort.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "删除成功");
    }

    @OperationLogger(value = "置顶资源分类")
    @ApiOperation(value = "置顶分类", notes = "置顶分类", response = String.class)
    @PostMapping("/stick")
    public String stick(HttpServletRequest request,
                        @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        ResourceCatalog resourceSort = resourceCatalogService.getById(uid);

        //查找出最大的那一个
        QueryWrapper<ResourceCatalog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConfiguration.SORT);
        Page<ResourceCatalog> page = new Page<>();
        page.setCurrent(0);
        page.setSize(1);
        IPage<ResourceCatalog> pageList = resourceCatalogService.page(page, queryWrapper);
        List<ResourceCatalog> list = pageList.getRecords();
        ResourceCatalog maxSort = list.get(0);

        if (StringUtils.isEmpty(maxSort.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        if (maxSort.getUid().equals(resourceSort.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "该分类已经在顶端");
        }

        Integer sortCount = maxSort.getSort() + 1;

        resourceSort.setSort(sortCount);

        resourceSort.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, "置顶成功");
    }
}

