package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.fantasizer.theblog.xo.entity.Picture;
import org.fantasizer.theblog.xo.entity.PictureCatalog;
import org.fantasizer.theblog.xo.service.PictureCatalogService;
import org.fantasizer.theblog.xo.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/picture")
public class PictureRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    PictureService pictureService;
    @Autowired
    PictureCatalogService pictureCatalogService;
    @Autowired
    private PictureFeignClient pictureFeignClient;
    @Value(value = "${data.image.url}")
    private String IMG_HOST;

    @ApiOperation(value = "获取图片列表", notes = "获取图片列表", response = String.class)
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public String getList(HttpServletRequest request,
                          @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                          @ApiParam(name = "pictureSortUid", value = "图片分类UID", required = true) @RequestParam(name = "pictureSortUid", required = true) String pictureSortUid,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "14") Long pageSize) {

        if (StringUtils.isEmpty(pictureSortUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(keyword) && !StringUtils.isEmpty(keyword.trim())) {
            queryWrapper.like(SQLConfiguration.PIC_NAME, keyword.trim());
        }

        Page<Picture> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.eq(SQLConfiguration.PICTURE_SORT_UID, pictureSortUid);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        IPage<Picture> pageList = pictureService.page(page, queryWrapper);
        List<Picture> pictureList = pageList.getRecords();

        final StringBuffer fileUids = new StringBuffer();
        pictureList.forEach(item -> {
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

        for (Picture item : pictureList) {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                log.info(pictureMap.get(item.getFileUid()));
                item.setPictureUrl(pictureMap.get(item.getFileUid()));
            }
        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加图片")
    @ApiOperation(value = "增加图片", notes = "增加图片", response = String.class)
    @PostMapping("/add")
    public String add(HttpServletRequest request,
                      @ApiParam(name = "fileUids", value = "图片UIDs", required = false) @RequestParam(name = "fileUids", required = false) String fileUids,
                      @ApiParam(name = "pictureSortUid", value = "图片分类UID", required = false) @RequestParam(name = "pictureSortUid", required = false) String pictureSortUid) {

        if (StringUtils.isEmpty(fileUids) || StringUtils.isEmpty(pictureSortUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        List<String> list = StringUtils.changeStringToString(fileUids, ",");
        if (list.size() > 0) {
            for (String fileUid : list) {
                Picture picture = new Picture();
                picture.setFileUid(fileUid);
                picture.setPictureSortUid(pictureSortUid);
                picture.setStatus(Status.ENABLED);
                picture.insert();
            }
        }


        return ResultUtil.result(SystemConfiguration.SUCCESS, "添加成功");
    }

    @OperationLogger(value = "编辑图片")
    @ApiOperation(value = "编辑图片", notes = "编辑图片", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request,
                       @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid,
                       @ApiParam(name = "fileUid", value = "图片UID", required = false) @RequestParam(name = "fileUid", required = false) String fileUid,
                       @ApiParam(name = "picName", value = "图片名称", required = false) @RequestParam(name = "picName", required = false) String picName,
                       @ApiParam(name = "pictureSortUid", value = "图片分类UID", required = false) @RequestParam(name = "pictureSortUid", required = false) String pictureSortUid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        Picture picture = pictureService.getById(uid);
        picture.setFileUid(fileUid);
        picture.setPicName(picName);
        picture.setPictureSortUid(pictureSortUid);
        picture.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, "编辑成功");
    }

    @OperationLogger(value = "删除图片")
    @ApiOperation(value = "删除图片", notes = "删除图片", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @ApiParam(name = "uid", value = "唯一UID", required = true) @RequestParam(name = "uid", required = true) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }
        List<String> uids = StringUtils.changeStringToString(uid, ",");
        for (String item : uids) {
            Picture picture = pictureService.getById(item);
            picture.setStatus(Status.DELETED);
            picture.updateById();
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, "删除成功");
    }

    @OperationLogger(value = "通过图片Uid将图片设为封面")
    @ApiOperation(value = "通过图片Uid将图片设为封面", notes = "通过图片Uid将图片设为封面", response = String.class)
    @PostMapping("/setCover")
    public String setCover(HttpServletRequest request,
                           @ApiParam(name = "pictureUid", value = "图片UID", required = true) @RequestParam(name = "pictureUid", required = true) String pictureUid,
                           @ApiParam(name = "pictureSortUid", value = "图片分类UID", required = true) @RequestParam(name = "pictureSortUid", required = true) String pictureSortUid) {

        if (StringUtils.isEmpty(pictureUid) || StringUtils.isEmpty(pictureSortUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "数据错误");
        }

        PictureCatalog pictureSort = pictureCatalogService.getById(pictureSortUid);

        if (pictureSort != null) {

            Picture picture = pictureService.getById(pictureUid);

            if (picture != null) {
                pictureSort.setFileUid(picture.getFileUid());
                pictureSort.updateById();
            } else {
                return ResultUtil.result(SystemConfiguration.ERROR, "找不到该图片");
            }


        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, "找不到该图片分类");
        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, "设置成功");
    }
}

