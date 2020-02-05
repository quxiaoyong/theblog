package org.fantasizer.theblog.common.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fantasizer.theblog.common.exception.ApiInvalidParamException;
import org.fantasizer.theblog.common.exception.BusinessException;
import org.fantasizer.theblog.common.exception.ErrorMessageHelper;
import org.fantasizer.theblog.common.global.ErrorConstants;
import org.fantasizer.theblog.common.helper.JsonHelper;
import org.fantasizer.theblog.common.vo.Result;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 异常解决
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:09
 */
public class TheBlogHandlerExceptionResolver implements HandlerExceptionResolver {

    private Log log = LogFactory.getLog(TheBlogHandlerExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        log.error("系统统一异常处理：", exception);

        // 若响应已响应或已关闭，则不操作
        if (response.isCommitted()) {
            return new ModelAndView();
        }

        // 组装错误提示信息
        String errorCode = exception instanceof BusinessException ? ((BusinessException) exception).getCode() : ErrorConstants.OPERATION_FAIL;
        String message = ErrorMessageHelper.getErrorMessage(errorCode, null);
        if (exception instanceof ApiInvalidParamException) {
            //定义错误编码
            //errorCode = 10001;
            message = exception.getMessage();
        }
        // 响应类型设置
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");

        // 响应结果输出
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JsonHelper.objectToJson(Result.createWithErrorMessage(message, errorCode)));
        } catch (Exception e) {
            log.error("响应输出失败！原因如下：", e);
        }
        return new ModelAndView();
    }
}
