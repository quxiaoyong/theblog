package org.fantasizer.theblog.admin.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fantasizer.theblog.common.enums.Status;
import org.fantasizer.theblog.common.helper.DateUtils;
import org.fantasizer.theblog.common.helper.ResultUtil;
import org.fantasizer.theblog.common.helper.StringUtils;
import org.fantasizer.theblog.admin.global.SQLConfiguration;
import org.fantasizer.theblog.admin.global.SystemConfiguration;
import org.fantasizer.theblog.xo.entity.ExceptionLog;
import org.fantasizer.theblog.xo.entity.SystemLog;
import org.fantasizer.theblog.xo.service.ExceptionLogService;
import org.fantasizer.theblog.xo.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Api(value = "管理员操作日志RestApi", tags = {"LogRestApi"})
@RequestMapping("/log")
public class LogRestApi {

    private static Logger log = LogManager.getLogger(AdministratorRestApi.class);
    @Autowired
    SystemLogService systemLogService;
    @Autowired
    ExceptionLogService exceptionLogService;

    @ApiOperation(value = "获取操作日志列表", notes = "获取操作日志列表", response = String.class)
    @RequestMapping(value = "/getLogList", method = RequestMethod.GET)
    public String getLogList(HttpServletRequest request,
                             @ApiParam(name = "userName", value = "用户名", required = false) @RequestParam(name = "userName", required = false) String userName,
                             @ApiParam(name = "operation", value = "接口名", required = false) @RequestParam(name = "operation", required = false) String operation,
                             @ApiParam(name = "startTime", value = "时间段", required = false) @RequestParam(name = "startTime", required = false) String startTime,
                             @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                             @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(userName) && !StringUtils.isEmpty(userName.trim())) {
            queryWrapper.like(SQLConfiguration.USER_NAME, userName.trim());
        }

        if (!StringUtils.isEmpty(operation)) {
            queryWrapper.like(SQLConfiguration.OPERATION, operation);
        }

        if (!StringUtils.isEmpty(startTime)) {
            String[] time = startTime.split(",");
            if (time.length < 2) {
                return ResultUtil.result(SystemConfiguration.ERROR, "传入时间有误");
            }
            queryWrapper.between(SQLConfiguration.CREATE_TIME, DateUtils.str2Date(time[0]), DateUtils.str2Date(time[1]));
        }

        Page<SystemLog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SQLConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SQLConfiguration.CREATE_TIME);
        IPage<SystemLog> pageList = systemLogService.page(page, queryWrapper);
        log.info("返回结果");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }

    @ApiOperation(value = "获取系统异常列表", notes = "获取系统异常列表", response = String.class)
    @RequestMapping(value = "/getExceptionList", method = RequestMethod.GET)
    public String getExceptionList(HttpServletRequest request,
                                   @ApiParam(name = "keyword", value = "关键字", required = false) @RequestParam(name = "keyword", required = false) String keyword,
                                   @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                   @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        QueryWrapper<ExceptionLog> queryWrapper = new QueryWrapper<ExceptionLog>();
        if (!StringUtils.isEmpty(keyword)) {
            queryWrapper.like(SystemConfiguration.CONTENT, keyword);
        }
        Page<ExceptionLog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        queryWrapper.eq(SystemConfiguration.STATUS, Status.ENABLED);
        queryWrapper.orderByDesc(SystemConfiguration.CREATE_TIME);
        queryWrapper.select(ExceptionLog.class, i -> !"exception_json".equals(i.getProperty()));
        IPage<ExceptionLog> pageList = exceptionLogService.page(page, queryWrapper);
        log.info("返回结果");
        return ResultUtil.result(SystemConfiguration.SUCCESS, pageList);
    }
}

