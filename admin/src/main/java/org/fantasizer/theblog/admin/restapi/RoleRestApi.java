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
import org.fantasizer.theblog.common.validator.group.Delete;
import org.fantasizer.theblog.common.validator.group.FetchList;
import org.fantasizer.theblog.common.validator.group.Insert;
import org.fantasizer.theblog.common.validator.group.Update;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Role;
import org.fantasizer.theblog.xo.service.RoleService;
import org.fantasizer.theblog.xo.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
@Api(value = "角色管理RestApi", tags = {"RoleRestApi"})
public class RoleRestApi {

    private static Logger log = LogManager.getLogger(RoleRestApi.class);
    @Autowired
    private RoleService roleService;

    @ApiOperation(value = "获取角色信息列表", notes = "获取角色信息列表")
    @PostMapping("/getList")
    public String getList(@Validated({FetchList.class}) @RequestBody RoleVO roleVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<Role> queryWrapper = new QueryWrapper<Role>();
        if (StringUtils.isNotEmpty(roleVO.getKeyword()) && StringUtils.isNotEmpty(roleVO.getKeyword().trim())) {
            queryWrapper.like(SQLConfiguration.ROLENAEM, roleVO.getKeyword().trim());
        }
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        Page<Role> page = new Page<>();
        page.setCurrent(roleVO.getCurrentPage());
        page.setSize(roleVO.getPageSize());
        IPage<Role> pageList = roleService.page(page, queryWrapper);
        log.info("获取角色信息列表");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "新增角色信息")
    @ApiOperation(value = "新增角色信息", notes = "新增角色信息")
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody RoleVO roleVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        String roleName = roleVO.getRoleName();
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConfiguration.ROLENAEM, roleName);
        Role getRole = roleService.getOne(queryWrapper);
        if (getRole == null) {
            Role role = new Role();
            role.setRoleName(roleVO.getRoleName());
            role.setCategoryMenuUids(roleVO.getCategoryMenuUids());
            role.setSummary(roleVO.getSummary());
            role.insert();
            return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
        }
        return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.ENTITY_EXIST);
    }

    @OperationLogger(value = "更新角色信息")
    @ApiOperation(value = "更新角色信息", notes = "更新角色信息")
    @PostMapping("/update")
    public String update(@Validated({Update.class}) @RequestBody RoleVO roleVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        String uid = roleVO.getUid();
        Role getRole = roleService.getById(uid);
        if (getRole == null) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.PARAM_INCORRECT);
        }
        getRole.setRoleName(roleVO.getRoleName());
        getRole.setCategoryMenuUids(roleVO.getCategoryMenuUids());
        getRole.setSummary(roleVO.getSummary());
        getRole.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);

    }

    @OperationLogger(value = "删除角色信息")
    @ApiOperation(value = "删除角色信息", notes = "删除角色信息")
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody RoleVO roleVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        Role role = roleService.getById(roleVO.getUid());
        role.setStatus(Status.DELETED);
        role.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }

}