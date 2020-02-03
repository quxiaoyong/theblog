package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.helper.WebUtils;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.ResourceCatalog;
import org.fantasizer.theblog.xo.entity.StudyVideo;
import org.fantasizer.theblog.xo.service.ResourceCatalogService;
import org.fantasizer.theblog.xo.service.StudyVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/studyVideo")
@Api(value = "视频RestApi", tags = {"StudyVideoRestApi"})
public class StudyVideoRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    StudyVideoService studyVideoService;
    @Autowired
    ResourceCatalogService resourceCatalogService;
    @Autowired
    PictureFeignClient pictureFeignClient;

    @ApiOperation(value = "获取学习视频列表", notes = "获取学习视频列表", response = String.class)
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public String getList(HttpServletRequest request,
                          @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<StudyVideo> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(keyword) && !StringUtils.isEmpty(keyword.trim())) {
            queryWrapper.like(SQLConfiguration.NAME, keyword.trim());
        }

        Page<StudyVideo> page = new Page<StudyVideo>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        IPage<StudyVideo> pageList = studyVideoService.page(page, queryWrapper);
        List<StudyVideo> list = pageList.getRecords();

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

        for (StudyVideo item : list) {
            //获取分类资源
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), ",");
                List<String> pictureListTemp = new ArrayList<String>();
                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }

            if (StringUtils.isNotEmpty(item.getResourceSortUid())) {
                ResourceCatalog resourceSort = resourceCatalogService.getById(item.getResourceSortUid());
                item.setResourceCatalog(resourceSort);
            }
        }
        log.info("返回结果");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加学习视频")
    @ApiOperation(value = "增加学习视频", notes = "增加学习视频", response = String.class)
    @PostMapping("/add")
    public String add(HttpServletRequest request, @RequestBody StudyVideo studyVideo) {

        if (StringUtils.isEmpty(studyVideo.getName()) || StringUtils.isEmpty(studyVideo.getResourceSortUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        studyVideo.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "添加成功");
    }

    @OperationLogger(value = "编辑学习视频")
    @ApiOperation(value = "编辑学习视频", notes = "编辑学习视频", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request, @RequestBody StudyVideo studyVideo) {

        if (StringUtils.isEmpty(studyVideo.getName()) || StringUtils.isEmpty(studyVideo.getResourceSortUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        studyVideo.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "编辑成功");
    }

    @OperationLogger(value = "删除学习视频")
    @ApiOperation(value = "删除学习视频", notes = "删除学习视频", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        StudyVideo studyVideo = studyVideoService.getById(uid);
        studyVideo.setStatus(Status.DELETED);
        studyVideo.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "删除成功");
    }
}

