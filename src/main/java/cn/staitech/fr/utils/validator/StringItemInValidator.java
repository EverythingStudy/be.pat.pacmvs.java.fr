package cn.staitech.fr.utils.validator;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * StringItemInValidator
 *
 * @author yxy
 * @since 2024-08-13
 */
public class StringItemInValidator implements ConstraintValidator<StringItemInAnnotation, String> {
    private List<String> list = new ArrayList<>();

    @Override
    public void initialize(StringItemInAnnotation constraintAnnotation) {
        String[] allowedValues = constraintAnnotation.allowedValues();
        if (allowedValues != null) {
            for (String v : allowedValues) {
                list.add(v);
            }
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (list.isEmpty()) {
            return true;
        }
        if (value == null) {
            return true;
        }
        return list.contains(value);
    }
}
