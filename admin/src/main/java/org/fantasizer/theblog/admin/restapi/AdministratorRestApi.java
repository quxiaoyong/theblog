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
import org.fantasizer.common.helper.CheckHelper;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.helper.WebUtils;
import org.fantasizer.theblog.admin.feign.PictureFeignClient;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Administrator;
import org.fantasizer.theblog.xo.entity.Role;
import org.fantasizer.theblog.xo.service.AdministratorService;
import org.fantasizer.theblog.xo.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 管理员表 RestApi
 * </p>
 *
 * @author xuzhixiang
 * @since 2018-09-04
 */
@RestController
@RequestMapping("/admin")
@Api(value = "管理员RestApi", tags = {"AdminRestApi"})
public class AdministratorRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AdministratorService administratorService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PictureFeignClient pictureFeignClient;
    @Value(value = "${DEFAULE_PWD}")
    private String defaultPwd;

    @ApiOperation(value = "获取管理员列表", notes = "获取管理员列表")
    @GetMapping("/getList")
    public String getList(HttpServletRequest request,
                          @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        String pictureResult = null;
        queryWrapper.like(SQLConfiguration.USER_NAME, keyword).or().like(SQLConfiguration.NICK_NAME, keyword.trim());
        Page<Administrator> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        // 去除密码
        queryWrapper.select(Administrator.class, i -> !i.getProperty().equals(SQLConfiguration.PASS_WORD));
        IPage<Administrator> pageList = administratorService.page(page, queryWrapper);
        List<Administrator> list = pageList.getRecords();
        log.info(list);

        final StringBuffer fileUids = new StringBuffer();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                fileUids.append(item.getAvatar() + SystemConfiguration.FILE_SEGMENTATION);
            }
        });

        Map<String, String> pictureMap = new HashMap<>();

        if (fileUids != null) {
            pictureResult = this.pictureFeignClient.getPicture(fileUids.toString(), SystemConfiguration.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = WebUtils.getPictureMap(pictureResult);

        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConfiguration.UID).toString(), item.get(SQLConfiguration.URL).toString());
        });

        for (Administrator item : list) {

            Role role = roleService.getById(item.getRoleUid());

            item.setRole(role);

            //获取图片
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getAvatar(), SystemConfiguration.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();
                pictureUidsTemp.forEach(picture -> {
                    if (pictureMap.get(picture) != null && pictureMap.get(picture) != "") {
                        pictureListTemp.add(pictureMap.get(picture));
                    }
                });
                item.setPhotoList(pictureListTemp);
            }
        }

        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "重置用户密码")
    @ApiOperation(value = "重置用户密码", notes = "重置用户密码")
    @PostMapping("/restPwd")
    public String restPwd(HttpServletRequest request,
                          @ApiParam(name = "uid", value = "管理员uid", required = true) @RequestParam(name = "uid", required = false) String uid) {

        if (StringUtils.isEmpty(uid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }

        Administrator admin = administratorService.getById(uid);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        admin.setPassWord(encoder.encode(defaultPwd));
        admin.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }

    @OperationLogger(value = "注册管理员")
    @ApiOperation(value = "注册管理员", notes = "注册管理员")
    @PostMapping("/add")
    public String add(HttpServletRequest request,
                      @ApiParam(name = "assignbody", value = "管理员注册对象", required = true) @RequestBody(required = true) Administrator registered) {

        String mobile = registered.getMobile();
        String userName = registered.getUserName();
        String email = registered.getEmail();

        if (StringUtils.isEmpty(userName)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }

        if (StringUtils.isEmpty(email) && StringUtils.isEmpty(mobile)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "邮箱和手机号至少一项不能为空");
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

            // 设置为未审核状态
            registered.setStatus(Status.ENABLED);

            PasswordEncoder encoder = new BCryptPasswordEncoder();

            //设置默认密码
            registered.setPassWord(encoder.encode(defaultPwd));

            administratorService.save(registered);

            //这里需要通过SMS模块，发送邮件告诉初始密码

            return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
        }
        return ResultUtil.result(SystemConfiguration.ERROR, "管理员账户已存在");
    }

    @OperationLogger(value = "更新管理员基本信息")
    @ApiOperation(value = "更新管理员基本信息", notes = "更新管理员基本信息")
    @PostMapping("/edit")
    public String edit(HttpServletRequest request,
                       @ApiParam(name = "updateBody", value = "管理员对象", required = true) @RequestBody(required = true) Administrator updateBody) {

        if (StringUtils.isEmpty(updateBody.getUid())) {
            return ResultUtil.result(SystemConfiguration.ERROR, "必填项不能为空");
        }
        Administrator admin = administratorService.getById(updateBody.getUid());
        if (admin != null) {
            //判断修改的对象是否是超级管理员，超级管理员不能修改用户名
            if (admin.getUserName().equals(SystemConfiguration.ADMIN) && !updateBody.getUserName().equals(SystemConfiguration.ADMIN)) {
                return ResultUtil.result(SystemConfiguration.ERROR, "超级管理员用户名必须为admin");
            }

            QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SQLConfiguration.USER_NAME, updateBody.getUserName()).or().eq(SQLConfiguration.EMAIL, updateBody.getEmail()).or().eq(SQLConfiguration.MOBILE, updateBody.getMobile());
            List<Administrator> adminList = administratorService.list(queryWrapper);
            if (adminList != null) {
                for (Administrator item : adminList) {
                    if (item.getUid().equals(updateBody.getUid())) {
                        continue;
                    } else {
                        return ResultUtil.result(SystemConfiguration.ERROR, "修改失败：用户名存在，手机号已注册，邮箱已经注册");
                    }
                }
            }

        }
        updateBody.setPassWord(null);
        updateBody.updateById();

        return ResultUtil.result(SystemConfiguration.SUCCESS, "更新管理员成功");
    }

    @OperationLogger(value = "更新管理员邮箱或手机号")
    @ApiOperation(value = "更新管理员邮箱或手机号", notes = "更新管理员邮箱或手机号")
    @PostMapping("/updateEmail")
    public String updateEmail(HttpServletRequest request,
                              @ApiParam(name = "newInfo", value = "管理员uid", required = true) @RequestParam(name = "adminUid", required = true) String adminUid,
                              @ApiParam(name = "newInfo", value = "管理员新邮箱或新手机号", required = true) @RequestParam(name = "newInfo", required = true) String newInfo,
                              @ApiParam(name = "validCode", value = "验验码", required = true) @RequestParam(name = "validCode", required = true) String validCode) {


        Administrator admin = administratorService.getById(adminUid);
        if (admin == null) {
            return ResultUtil.result(SystemConfiguration.ERROR, "管理员不存在");
        }

        //从redis中获取验证码
        String checkValidCode = stringRedisTemplate.opsForValue().get(newInfo);
        if (checkValidCode.isEmpty()) {
            return ResultUtil.result(SystemConfiguration.ERROR, "验证码已过期");
        }
        if (!checkValidCode.equals(validCode)) {
            return ResultUtil.result(SystemConfiguration.ERROR, "验证码不正确");
        }
        if (checkValidCode.equals(validCode)) {
            if (CheckHelper.checkEmail(newInfo)) {
                admin.setEmail(newInfo);
            } else if (CheckHelper.checkMobileNumber(newInfo)) {
                admin.setMobile(newInfo);
            } else {
                return ResultUtil.result(SystemConfiguration.ERROR, "输入的邮箱或手机号格式有误");
            }
            admin.setUpdateTime(new Date());
            administratorService.updateById(admin);
            //删除缓存中的验证码
            stringRedisTemplate.delete(newInfo);
            return ResultUtil.result(SystemConfiguration.SUCCESS, "更新成功");
        }
        return ResultUtil.result(SystemConfiguration.ERROR, "验证码错误");
    }

    @OperationLogger(value = "删除部分管理员信息")
    @ApiOperation(value = "删除部分管理员信息", notes = "删除部分管理员信息")
    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @ApiParam(name = "adminUids", value = "管理员uid集合", required = true) @RequestParam(name = "adminUids", required = true) List<String> adminUids) {
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper<>();
        if (adminUids.isEmpty()) {
            return ResultUtil.result(SystemConfiguration.ERROR, "管理员uid不能为空");
        }
        queryWrapper.in(SQLConfiguration.UID, adminUids);
        administratorService.remove(queryWrapper);
        return ResultUtil.result(SystemConfiguration.SUCCESS, "删除管理员成功");
    }

    @OperationLogger(value = "分配用户角色信息列表")
    @ApiOperation(value = "分配用户角色信息列表", notes = "分配用户角色信息列表")
    @PostMapping("/assign")
    public String assign(HttpServletRequest request,
                         @ApiParam(name = "adminUid", value = "管理员uid", required = true) @RequestParam(name = "adminUid", required = true) String adminUid) {

        Map<String, Object> map = new HashMap<>();

        Administrator admin = administratorService.getById(adminUid);
        map.put("admin", admin);
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        List<Role> roles = roleService.list(queryWrapper);

        List<Role> assignedRoles = new ArrayList<>();
        List<Role> unassignRoles = new ArrayList<>();

        //根据admin获取账户拥有的角色uid集合
        List<String> roleUids = new ArrayList<>();
        roleUids.add(admin.getRoleUid());
        for (Role role : roles) {
            if (roleUids.contains(role.getUid())) {
                assignedRoles.add(role);
            } else {
                unassignRoles.add(role);
            }
        }
        map.put("assignedRoles", assignedRoles);
        map.put("unassignRoles", unassignRoles);
        return ResultUtil.result(SystemConfiguration.SUCCESS, map);
    }

}

