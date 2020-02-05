package org.fantasizer.theblog.common.validator.annnotation;

import org.fantasizer.theblog.common.validator.Messages;
import org.fantasizer.theblog.common.validator.constraint.LongValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * TODO：Long和Integer应该可以考虑何为Number类型校验
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 21:37
 */
@Target({TYPE, ANNOTATION_TYPE, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {LongValidator.class})
public @interface LongNotNull {

    boolean required() default true;

    String message() default Messages.CK_NUMERIC_DEFAULT;

    String value() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
