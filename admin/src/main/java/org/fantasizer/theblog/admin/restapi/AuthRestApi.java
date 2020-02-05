package org.fantasizer.theblog.admin.restapi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.helper.CheckHelper;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.config.jwt.Audience;
import org.fantasizer.theblog.config.jwt.JwtHelper;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.service.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@Api(value = "需放行接口RestApi", tags = {"AuthRestApi"})
public class AuthRestApi {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private Audience audience;

    @Value(value = "${tokenHead}")
    private String tokenHead;

    @OperationLogger(value = "注册管理员")
    @ApiOperation(value = "注册管理员", notes = "注册管理员")
    @PostMapping("/register")
    public String register(HttpServletRequest request,
                           @ApiParam(name = "assignbody", value = "管理员注册对象", required = true) @RequestBody(required = true) Administrator registered) {

        String mobile = registered.getMobile();
        String userName = registered.getUserName();
        String email = registered.getEmail();
        String passWord = registered.getPassWord();
        String code = registered.getValidCode();
        String validCode = null;

        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "用户名或密码不能为空");
        }

        if (StringUtils.isEmpty(email) && StringUtils.isEmpty(mobile)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "邮箱和手机号至少一项不能为空");
        }

        //手机号为空时为邮箱注册
        if (StringUtils.isEmpty(mobile) && CheckHelper.checkEmail(email)) {
            /**
             * 从redis中获取验证码
             */
            validCode = stringRedisTemplate.opsForValue().get(email);
        } else if (StringUtils.isEmpty(email) && CheckHelper.checkMobileNumber(mobile)) {

            /**
             * 从redis中获取验证码
             */
            validCode = stringRedisTemplate.opsForValue().get(mobile);
        } else {
            return ResultUtil.result(SystemConfiguration.ERROR, "邮箱或手机号格式有误");
        }
        if (validCode.isEmpty()) {
            return ResultUtil.result(SystemConfiguration.ERROR, "验证码已过期");
        }

        if (code.equals(validCode)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "验证码不正确");
        }


        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.USER_NAME, userName);
        Administrator admin = administratorService.getOne(queryWrapper);

        QueryWrapper<Administrator> wrapper = new QueryWrapper<>();
        if (admin == null) {
            if (StringUtils.isNotEmpty(email)) {
                wrapper.eq(SQLConfiguration.EMAIL, email);
            } else {
                wrapper.eq(SQLConfiguration.MOBILE, mobile);
            }

            if (administratorService.getOne(wrapper) != null) {
                return ResultUtil.result(SystemConfiguration.ERROR, "管理员账户已存在");
            }

            /**
             * 设置为未审核状态
             */
            registered.setStatus(Status.DELETED);
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            registered.setPassWord(encoder.encode(registered.getPassWord()));
            administratorService.save(registered);
            //清除redis中的缓存
            if (StringUtils.isEmpty(mobile)) {
                stringRedisTemplate.delete(email);
            } else {
                stringRedisTemplate.delete(mobile);
            }
            return ResultUtil.result(SystemConfiguration.SUCCESS, "注册成功");
        }
        return ResultUtil.result(SystemConfiguration.ERROR, "管理员账户已存在");
    }

    @OperationLogger(value = "更新管理员密码")
    @ApiOperation(value = "更新管理员密码", notes = "更新管理员密码")
    @PostMapping("/updatePassWord")
    public String updatePassWord(HttpServletRequest request,
                                 @ApiParam(name = "userInfo", value = "管理员账户名", required = true) @RequestParam(name = "userInfo", required = true) String userInfo,
                                 @ApiParam(name = "passWord", value = "管理员旧密码", required = true) @RequestParam(name = "passWord", required = true) String passWord,
                                 @ApiParam(name = "newPassWord", value = "管理员新密码", required = true) @RequestParam(name = "newPassWord", required = true) String newPassWord) {
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        if (CheckHelper.checkEmail(userInfo)) {
            queryWrapper.eq(SQLConfiguration.EMAIL, userInfo);
        } else if (CheckHelper.checkMobileNumber(userInfo)) {
            queryWrapper.eq(SQLConfiguration.MOBILE, userInfo);
        } else {
            queryWrapper.eq(SQLConfiguration.USER_NAME, userInfo);
        }
        Administrator admin = administratorService.getOne(queryWrapper);
        if (admin == null) {
            return ResultUtil.result(SystemConfiguration.ERROR, "管理员不存在");
        }
        if (StringUtils.isEmpty(passWord)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "旧密码不能为空");
        }
        if (StringUtils.isEmpty(newPassWord)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "新密码不能为空");
        }
        String uid = admin.getUid();

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isPassword = encoder.matches(passWord, admin.getPassWord());
        if (isPassword) {
            admin.setPassWord(encoder.encode(newPassWord));
            UpdateWrapper<Administrator> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq(SQLConfiguration.UID, uid);
            admin.setUpdateTime(new Date());
            administratorService.update(admin, updateWrapper);
            return ResultUtil.result(SystemConfiguration.SUCCESS, "密码更新成功");
        }
        return ResultUtil.result(SystemConfiguration.ERROR, "旧密码错误");
    }

    @OperationLogger(value = "更新token")
    @ApiOperation(value = "更新token", notes = "更新token")
    @PostMapping("/refreshToken")
    public String refreshToken(String oldToken) {

        final String token = oldToken.substring(tokenHead.length());
        if (jwtHelper.canTokenBeRefreshed(token, audience.getBase64Secret())) {
            return jwtHelper.refreshToken(token, audience.getBase64Secret(), audience.getExpiresSecond());
        }
        return null;
    }
}
