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
import org.fantasizer.theblog.common.helper.WebUtils;
import org.fantasizer.theblog.common.validator.group.Delete;
import org.fantasizer.theblog.common.validator.group.FetchList;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.User;
import org.fantasizer.theblog.xo.service.UserService;
import org.fantasizer.theblog.xo.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 RestApi
 * </p>
 *
 * @author 陌溪
 * @since 2020年1月4日21:29:09
 */
@RestController
@Api(value = "用户RestApi", tags = {"UserRestApi"})
@RequestMapping("/user")
public class UserRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);

    @Autowired
    UserService userService;

    @Autowired
    private PictureFeignClient pictureFeignClient;

    @ApiOperation(value = "获取友链列表", notes = "获取友链列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(userVO.getKeyword()) && !StringUtils.isEmpty(userVO.getKeyword().trim())) {
            queryWrapper.like(SQLConfiguration.USER_NAME, userVO.getKeyword().trim());
        }
        queryWrapper.select(User.class, i -> !i.getProperty().equals(SQLConfiguration.PASS_WORD));
        Page<User> page = new Page<>();
        page.setCurrent(userVO.getCurrentPage());
        page.setSize(userVO.getPageSize());
        queryWrapper.ne(SQLConfiguration.STATUS, Status.DELETED);

        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        IPage<User> pageList = userService.page(page, queryWrapper);

        List<User> list = pageList.getRecords();

        final StringBuffer fileUids = new StringBuffer();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                fileUids.append(item.getAvatar() + SystemConfiguration.FILE_SEGMENTATION);
            }
        });

        Map<String, String> pictureMap = new HashMap<>();
        String pictureResult = null;

        if (fileUids != null) {
            pictureResult = this.pictureFeignClient.getPicture(fileUids.toString(), SystemConfiguration.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = WebUtils.getPictureMap(pictureResult);

        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConfiguration.UID).toString(), item.get(SQLConfiguration.URL).toString());
        });

        for (User item : list) {


            //获取图片
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getAvatar(), SystemConfiguration.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();
                pictureUidsTemp.forEach(picture -> {
                    if (pictureMap.get(picture) != null && pictureMap.get(picture) != "") {
                        pictureListTemp.add(pictureMap.get(picture));
                    }
                });
                if(pictureListTemp.size() > 0) {
                    item.setPhotoUrl(pictureListTemp.get(0));
                }
            }
        }

        log.info("获取用户列表");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }


    @OperationLogger(value = "删除用户")
    @ApiOperation(value = "删除用户", notes = "删除用户", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        User user = userService.getById(userVO.getUid());
        user.setStatus(Status.DELETED);
        user.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }

    @ApiOperation(value = "冻结/解冻用户", notes = "冻结/解冻用户", response = String.class)
    @PostMapping("/freeze")
    public String freezeUser(@Validated({Delete.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        User user = userService.getById(userVO.getUid());

        if(user.getStatus() == Status.FREEZE) {
            user.setStatus(Status.ENABLED);
        } else if(user.getStatus() == Status.DELETED) {
            user.setStatus(Status.DELETED);
        } else {
            user.setStatus(Status.FREEZE);
        }
        user.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.OPERATION_SUCCESS);
    }
}