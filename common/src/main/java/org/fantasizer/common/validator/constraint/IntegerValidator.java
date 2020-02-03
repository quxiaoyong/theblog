package org.fantasizer.common.validator.constraint;

import org.fantasizer.common.validator.annnotation.IntegerNotNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 整数不为空的校验
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:35
 */
public class IntegerValidator implements ConstraintValidator<IntegerNotNull, Integer> {

    @Override
    public void initialize(IntegerNotNull constraintAnnotation) {

    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return true;
    }

}
