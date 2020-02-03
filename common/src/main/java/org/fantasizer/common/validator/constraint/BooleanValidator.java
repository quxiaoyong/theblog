package org.fantasizer.common.validator.constraint;

import org.fantasizer.common.validator.annnotation.BooleanNotNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 21:22
 */
public class BooleanValidator implements ConstraintValidator<BooleanNotNull, Boolean> {

    @Override
    public void initialize(BooleanNotNull constraintAnnotation) {

    }

    @Override
    public boolean isValid(Boolean value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return true;
    }

}
