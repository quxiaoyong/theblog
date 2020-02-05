package org.fantasizer.theblog.admin.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.fantasizer.theblog.common.helper.IpUtils;
import org.fantasizer.theblog.config.security.SecurityUser;
import org.fantasizer.theblog.xo.entity.ExceptionLog;
import org.fantasizer.theblog.xo.entity.SystemLog;
import org.fantasizer.theblog.xo.service.ExceptionLogService;
import org.fantasizer.theblog.xo.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 日志切面
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:48
 */
@Aspect
@Component
public class LoggerAspect {

    private SystemLog systemLog;

    private ExceptionLog exceptionLog;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private ExceptionLogService exceptionLogService;

    @Autowired
    private HttpServletRequest request;

    @Pointcut(value = "@annotation(operationLogger)")
    public void pointcut(OperationLogger operationLogger) {

    }

    /**
     * 前置通知
     *
     * @param joinPoint
     * @param operationLogger
     */
    @Before(value = "pointcut(operationLogger)")
    public void doBefore(JoinPoint joinPoint, OperationLogger operationLogger) {
        systemLog = new SystemLog();

        //获取切入点参数

        //获取ip地址
        String ip = IpUtils.getIpAddr(request);
        //设置请求信息
        systemLog.setIp(ip);
        //设置调用的类
        systemLog.setClassPath(joinPoint.getTarget().getClass().getName());
        //设置调用的方法
        systemLog.setMethod(joinPoint.getSignature().getName());
        //设置Request的请求方式 GET POST
        systemLog.setType(request.getMethod());
        Object[] o = joinPoint.getArgs();
        String params = "";
        for (int a = 0; a < o.length; a++) {
            params = params + "参数" + (a + 1) + ":" + o[a] + ", ";
        }
        systemLog.setParams(params);

        systemLog.setUrl(request.getRequestURI().toString());

        systemLog.setOperation(operationLogger.value());
    }

    @AfterReturning(value = "pointcut(operationLogger)")
    public void doAfterReturning(OperationLogger operationLogger) {
        systemLog.setCreateTime(new Date());
        systemLog.setUpdateTime(new Date());
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        systemLog.setUserName(securityUser.getUsername());
        systemLog.setAdminUid(securityUser.getUid());
        systemLogService.save(systemLog);
    }

    @AfterThrowing(value = "pointcut(operationLogger)", throwing = "e")
    public void doAfterThrowing(OperationLogger operationLogger, Throwable e) {
        exceptionLog = new ExceptionLog();
        //设置异常信息
        exceptionLog.setCreateTime(new Date());
        exceptionLog.setExceptionJson(JSON.toJSONString(e,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteMapNullValue));
        exceptionLog.setExceptionMessage(e.getMessage());

        //保存异常日志信息
        exceptionLogService.save(exceptionLog);
    }


}