package org.fantasizer.theblog.common.validator.constraint;

import org.apache.commons.lang3.StringUtils;
import org.fantasizer.theblog.common.validator.Messages;
import org.fantasizer.theblog.common.validator.annnotation.Range;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 数字范围校验，可用来做文章或评论长度校验。
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:44
 */
public class RangValidator implements ConstraintValidator<Range, String> {

    /**
     * TODO:这个默认最大的意义到底有多大，值得考虑
     */
    private final int DEFAULT_MAX = 11;

    private long min;

    private long max;

    @Override
    public void initialize(Range constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (null == value || StringUtils.isBlank(value)) {
            return false;
        }
        // 限制长度最大11
        if (value.length() > DEFAULT_MAX) {
            String template = String.format(Messages.CK_RANG_MESSAGE_LENGTH_TYPE, value);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(template).addConstraintViolation();
            return false;
        }
        // 是否可数字化
        if (!StringUtils.isNumeric(value)) {
            String template = String.format(Messages.CK_NUMERIC_TYPE, value);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(template).addConstraintViolation();
            return false;
        }
        long l = Long.parseLong(value);
        return l >= min && l <= max;
    }
}