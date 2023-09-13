package sk.tuke.gamestudio.server.dto.validators;

import sk.tuke.gamestudio.server.dto.UserDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class ListNotEmptyValidator implements ConstraintValidator<ListNotEmpty, Object> {

    @Override
    public void initialize(final ListNotEmpty constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        final List<?> user = (List<?>) obj;
        return user.size() > 0;
    }
}