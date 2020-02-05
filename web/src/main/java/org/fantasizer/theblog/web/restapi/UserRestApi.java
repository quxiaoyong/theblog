package org.fantasizer.theblog.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fantasizer.theblog.common.exception.ThrowableHelper;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.validator.group.FetchOne;
import org.fantasizer.theblog.common.validator.group.Insert;
import org.fantasizer.theblog.web.global.SQLConfiguration;
import org.fantasizer.theblog.web.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.User;
import org.fantasizer.theblog.xo.service.UserService;
import org.fantasizer.theblog.xo.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(value = "登录管理RestApi", tags = {"loginRestApi"})
public class UserRestApi {


    @Autowired
    private UserService userService;


    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public String login(@Validated({FetchOne.class}) @RequestBody UserVO userVO, BindingResult result) {

        ThrowableHelper.checkParamArgument(result);

        return ResultUtil.result(SystemConfiguration.SUCCESS, "我登录啦" + userVO.getEmail());
    }

    @ApiOperation(value = "用户注册", notes = "用户登录")
    @PostMapping("/register")
    public String register(@Validated({Insert.class}) @RequestBody UserVO userVO, BindingResult result) {

        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.USER_NAME, userVO.getUserName());
        User user = userService.getOne(queryWrapper);

        if (user != null) {
            return ResultUtil.result(SystemConfiguration.ERROR, "用户已存在");
        }

        user = new User();
        user.setUserName(userVO.getUserName());
        user.setPassword(userVO.getPassword());
        user.setEmail(userVO.getEmail());


        return ResultUtil.result(SystemConfiguration.SUCCESS, "我注册啦" + userVO.getEmail());
    }


    @ApiOperation(value = "退出登录", notes = "退出登录", response = String.class)
    @PostMapping(value = "/logout")
    public String logout(@ApiParam(name = "token", value = "token令牌", required = false) @RequestParam(name = "token", required = false) String token) {
        String destroyToken = null;
        return ResultUtil.result(SystemConfiguration.SUCCESS, destroyToken);
    }

}
