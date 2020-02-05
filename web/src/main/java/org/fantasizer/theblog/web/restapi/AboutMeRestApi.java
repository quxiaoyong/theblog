package org.fantasizer.theblog.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.common.helper.WebUtils;
import org.fantasizer.theblog.web.feign.PictureFeignClient;
import org.fantasizer.theblog.web.global.SQLConfiguration;
import org.fantasizer.theblog.web.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.service.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/about")
@Api(value = "关于我 RestApi", tags = {"AboutMeRestApi"})
public class AboutMeRestApi {

    private static Logger log = LogManager.getLogger(AboutMeRestApi.class);
    @Autowired
    AdministratorService administratorService;
    @Autowired
    private PictureFeignClient pictureFeignClient;

    /**
     * 获取关于我的信息
     *
     * @author xzx19950624@qq.com
     * @date 2018年11月6日下午8:57:48
     */

    @ApiOperation(value = "关于我", notes = "关于我")
    @GetMapping("/getMe")
    public String getMe(HttpServletRequest request) {

        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.USER_NAME, SystemConfiguration.ADMIN);
        Administrator admin = administratorService.getOne(queryWrapper);
        admin.setPassWord(null); //清空密码，防止泄露
        //获取图片
        if (StringUtils.isNotEmpty(admin.getAvatar())) {
            String pictureList = this.pictureFeignClient.getPicture(admin.getAvatar(), ",");
            admin.setPhotoList(WebUtils.getPicture(pictureList));
        }
        log.info("获取用户信息");
        Administrator result = new Administrator();
        result.setNickName(admin.getNickName());
        result.setOccupation(admin.getOccupation());
        result.setSummary(admin.getSummary());
        result.setWeChat(admin.getWeChat());
        result.setQqNumber(admin.getQqNumber());
        result.setEmail(admin.getEmail());
        result.setMobile(admin.getMobile());
        result.setAvatar(admin.getAvatar());
        result.setPhotoList(admin.getPhotoList());
        result.setGithub(admin.getGithub());
        result.setGitee(admin.getGitee());
        return ResultUtil.result(SystemConfiguration.SUCCESS, result);
    }

    @ApiOperation(value = "获取联系方式", notes = "获取联系方式")
    @GetMapping("/getContact")
    public String getContact(HttpServletRequest request) {

        Administrator admin = administratorService.getById("1f01cd1d2f474743b241d74008b12333");

        if (admin != null) {

            Administrator result = new Administrator();
            result.setWeChat(admin.getWeChat());
            result.setQqNumber(admin.getQqNumber());
            result.setEmail(admin.getEmail());
            result.setMobile(admin.getMobile());
            result.setGithub(admin.getGithub());
            result.setGitee(admin.getGitee());
            return ResultUtil.result(SystemConfiguration.SUCCESS, result);
        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, "获取失败");
        }

    }


}

