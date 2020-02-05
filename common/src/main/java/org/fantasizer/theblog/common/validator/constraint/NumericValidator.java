package org.fantasizer.theblog.common.validator.constraint;

import org.apache.commons.lang3.StringUtils;
import org.fantasizer.theblog.common.validator.annnotation.Numeric;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 21:43
 */
public class NumericValidator implements ConstraintValidator<Numeric, String> {
    @Override
    public void initialize(Numeric constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || StringUtils.isBlank(value)) {
            return false;
        }

        return StringUtils.isNumeric(value);

    }
}
