package cn.staitech.fr.utils.validator;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * StringItemInAnnotation
 *
 * @author yxy
 * @since 2024-08-13
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StringItemInValidator.class)
public @interface StringItemInAnnotation {
    String[] allowedValues();

    String message() default "请检查值是否在allowedValues内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
