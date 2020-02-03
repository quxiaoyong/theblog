package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.common.enums.Status;
import org.fantasizer.common.exception.ThrowableHelper;
import org.fantasizer.common.helper.ResultUtil;
import org.fantasizer.common.helper.StringUtils;
import org.fantasizer.common.validator.group.*;
import org.fantasizer.theblog.admin.global.MessageConfiguration;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.admin.log.OperationLogger;
import org.fantasizer.theblog.xo.entity.Todo;
import org.fantasizer.theblog.xo.service.TodoService;
import org.fantasizer.theblog.xo.vo.TodoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
@RestController
@Api(value = "代办事项RestApi", tags = {"LinkRestApi"})
@RequestMapping("/todo")
public class TodoRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    TodoService todoService;

    @ApiOperation(value = "获取代办事项列表", notes = "获取代办事项列表", response = String.class)
    @PostMapping("/getList")
    public String getList(HttpServletRequest request, @Validated({FetchList.class}) @RequestBody TodoVO todoVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(todoVO.getKeyword()) && !StringUtils.isEmpty(todoVO.getKeyword().trim())) {
            queryWrapper.like(SQLConfiguration.TEXT, todoVO.getKeyword().trim());
        }

        String adminUid = request.getAttribute(SystemConfiguration.ADMIN_UID).toString();

        queryWrapper.eq(SQLConfiguration.ADMINUID, adminUid);

        //按时间顺序倒排
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);

        Page<Todo> page = new Page<>();
        page.setCurrent(todoVO.getCurrentPage());
        page.setSize(todoVO.getPageSize());
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        IPage<Todo> pageList = todoService.page(page, queryWrapper);
        log.info("执行获取代办事项列表");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @OperationLogger(value = "增加代办事项")
    @ApiOperation(value = "增加代办事项", notes = "增加代办事项", response = String.class)
    @PostMapping("/add")
    public String add(HttpServletRequest request, @Validated({Insert.class}) @RequestBody TodoVO todoVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        String adminUid = request.getAttribute(SystemConfiguration.ADMIN_UID).toString();
        Todo todo = new Todo();
        todo.setText(todoVO.getText());
        //默认未完成
        todo.setDone(false);
        todo.setAdminUid(adminUid);
        todo.insert();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.INSERT_SUCCESS);
    }

    @OperationLogger(value = "编辑代办事项")
    @ApiOperation(value = "编辑代办事项", notes = "编辑代办事项", response = String.class)
    @PostMapping("/edit")
    public String edit(HttpServletRequest request, @Validated({Update.class}) @RequestBody TodoVO todoVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        String adminUid = request.getAttribute(SystemConfiguration.ADMIN_UID).toString();

        Todo todo = todoService.getById(todoVO.getUid());

        if (!todo.getAdminUid().equals(adminUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.ACCESS_NO_PRIVILEGE);
        }

        todo.setText(todoVO.getText());
        todo.setDone(todoVO.getDone());
        todo.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }

    @OperationLogger(value = "删除代办事项")
    @ApiOperation(value = "删除代办事项", notes = "删除代办事项", response = String.class)
    @PostMapping("/delete")
    public String delete(HttpServletRequest request, @Validated({Delete.class}) @RequestBody TodoVO todoVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        String adminUid = request.getAttribute(SystemConfiguration.ADMIN_UID).toString();

        Todo todo = todoService.getById(todoVO.getUid());

        if (!todo.getAdminUid().equals(adminUid)) {
            return ResultUtil.result(SystemConfiguration.ERROR, MessageConfiguration.DATA_NO_PRIVILEGE);
        }

        todo.setStatus(Status.DELETED);
        todo.updateById();
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.DELETE_SUCCESS);
    }

    @OperationLogger(value = "批量编辑代办事项")
    @ApiOperation(value = "批量编辑代办事项", notes = "批量编辑代办事项", response = String.class)
    @PostMapping("/toggleAll")
    public String toggleAll(HttpServletRequest request, @Validated({FetchOne.class}) @RequestBody TodoVO todoVO, BindingResult result) {

        // 参数校验
        ThrowableHelper.checkParamArgument(result);

        String adminUid = request.getAttribute(SystemConfiguration.ADMIN_UID).toString();

        if (todoVO.getDone()) {
            todoService.toggleAll(SystemConfiguration.ONE, adminUid);
        } else {
            todoService.toggleAll(SystemConfiguration.ZERO, adminUid);
        }
        return ResultUtil.result(SystemConfiguration.SUCCESS, MessageConfiguration.UPDATE_SUCCESS);
    }


}

