package sk.tuke.gamestudio.server.dto.validators;

import sk.tuke.gamestudio.server.dto.UserDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        /*final UserDto user = (UserDto) obj;
        return user.getPassword() != null ? user.getPassword().equals(user.getMatchingPassword()) : true;*/
        return true;
    }
}