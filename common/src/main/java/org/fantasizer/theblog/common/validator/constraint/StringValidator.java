package org.fantasizer.theblog.common.validator.constraint;

import org.apache.commons.lang.StringUtils;
import org.fantasizer.theblog.common.validator.annnotation.StringNotNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 21:41
 */
public class StringValidator implements ConstraintValidator<StringNotNull, String> {
    @Override
    public void initialize(StringNotNull constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || StringUtils.isBlank(value) || StringUtils.isEmpty(value.trim())) {
            return false;
        }
        return true;
    }
}
