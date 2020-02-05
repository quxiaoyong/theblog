package org.fantasizer.theblog.common.exception;

import cn.hutool.core.collection.CollectionUtil;
import org.fantasizer.theblog.common.global.Constants;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Objects;

/**
 * TODO:感觉这个类设计得不太合理
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:04
 */
public class ThrowableHelper {

    private ThrowableHelper() {

    }

    /**
     * 校验参数正确,拼装字段名和值到错误信息
     *
     * @param result
     */
    public static void checkParamArgument(BindingResult result) {
        if (result != null && result.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> errors = result.getFieldErrors();
            if (CollectionUtil.isNotEmpty(errors)) {
                FieldError error = errors.get(0);
                String rejectedValue = Objects.toString(error.getRejectedValue(), "");
                String defMsg = error.getDefaultMessage();
                /**
                 * 排除类上面的注解提示
                 */
                if (rejectedValue.contains(Constants.DELIMITER_TO)) {
                    // 自己去确定错误字段
                    sb.append(defMsg);
                } else {
                    if (Constants.DELIMITER_COLON.contains(defMsg)) {
                        sb.append(error.getField()).append(" ").append(defMsg);
                    } else {
                        sb.append(error.getField()).append(" ").append(defMsg);
                    }
                }
            } else {
                String msg = result.getAllErrors().get(0).getDefaultMessage();
                sb.append(msg);
            }
            throw new ApiInvalidParamException(sb.toString());
        }

    }
}
