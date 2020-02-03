package org.fantasizer.theblog.admin.restapi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.helper.WebUtils;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.service.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/system")
@Api(value = "系统设置RestApi", tags = {"SystemRestApi"})
public class SystemRestApi {

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

    @ApiOperation(value = "获取我的信息", notes = "获取我的信息")
    @GetMapping("/getMe")
    public String getMe(HttpServletRequest request) {

        if (request.getAttribute(SystemConfiguration.ADMIN_UID) == null || request.getAttribute(SystemConfiguration.ADMIN_UID) == "") {
            return ResultUtil.result(SystemConfiguration.ERROR, "登录失效，请重新登录");
        }

        Administrator admin = administratorService.getById(request.getAttribute(SystemConfiguration.ADMIN_UID).toString());
        admin.setPassWord(null);

        //获取图片
        if (StringUtils.isNotEmpty(admin.getAvatar())) {
            String pictureList = this.pictureFeignClient.getPicture(admin.getAvatar(), ",");
            admin.setPhotoList(WebUtils.getPicture(pictureList));
        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, admin);
    }

    @OperationLogger(value = "编辑我的信息")
    @ApiOperation(value = "编辑我的信息", notes = "获取我的信息")
    @PostMapping("/editMe")
    public String editMe(HttpServletRequest request, @RequestBody Administrator admin) {

        Boolean save = administratorService.updateById(admin);

        return ResultUtil.result(SystemConfiguration.SUCCESS, save);
    }

    @ApiOperation(value = "修改密码", notes = "修改密码")
    @PostMapping("/changePwd")
    public String changePwd(HttpServletRequest request,
                            @ApiParam(name = "oldPwd", value = "旧密码", required = false) @RequestParam(name = "oldPwd", required = false) String oldPwd,
                            @ApiParam(name = "newPwd", value = "新密码", required = false) @RequestParam(name = "newPwd", required = false) String newPwd) throws NoSuchAlgorithmException {

        if (request.getAttribute(SystemConfiguration.ADMIN_UID) == null || request.getAttribute(SystemConfiguration.ADMIN_UID) == "") {
            return ResultUtil.result(SystemConfiguration.ERROR, "登录失效，请重新登录");
        }
        if (StringUtils.isEmpty(oldPwd) || StringUtils.isEmpty(newPwd)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }

        Administrator admin = administratorService.getById(request.getAttribute(SystemConfiguration.ADMIN_UID).toString());

        PasswordEncoder encoder = new BCryptPasswordEncoder();

        boolean isPassword = encoder.matches(oldPwd, admin.getPassWord());

        if (isPassword) {
            admin.setPassWord(encoder.encode(newPwd));
            admin.updateById();
            return ResultUtil.result(SystemConfiguration.SUCCESS, "修改成功");
        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, "输入密码错误");
        }

    }

}
