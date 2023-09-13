package sk.tuke.gamestudio.server.dto.validators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sk.tuke.gamestudio.MVCConfig;
import sk.tuke.gamestudio.server.dto.UserDto;

import java.util.Set;

import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(classes = {MVCConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ListValidatorTest {
    @Autowired
    private Validator validator;

    @Test
    public void shouldBeViolationWhenPasswordsDoNotMatch() throws Exception {
        ValidList<UserDto> users = new ValidList<>();
        users.add(new UserDto("TestUserName", "testPassword"));
        users.add(new UserDto());
        Set<ConstraintViolation<ValidList<UserDto>>> violations = validator.validate(users);

        assertFalse(violations.isEmpty());
    }
}
