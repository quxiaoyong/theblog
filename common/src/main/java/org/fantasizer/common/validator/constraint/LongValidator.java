package org.fantasizer.common.validator.constraint;

import org.fantasizer.common.validator.annnotation.LongNotNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Long类型校验
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:37
 */
public class LongValidator implements ConstraintValidator<LongNotNull, Long> {
    @Override
    public void initialize(LongNotNull constraintAnnotation) {

    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return true;
    }
}
