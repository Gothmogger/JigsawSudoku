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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(classes = {MVCConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ListNotEmptyValidatorTest {
    @Autowired
    private Validator validator;

    @Test
    public void shouldBeViolationWhenListIsEmpty() throws Exception {
        ValidList<UserDto> users = new ValidList<>();
        Set<ConstraintViolation<ValidList<UserDto>>> violations = validator.validate(users);

        assertFalse(violations.isEmpty());
    }

    @Test
    public void shouldBeNoViolationWhenListIsNotEmpty() {
        List<UserDto> users = new ArrayList<>();
        Set<ConstraintViolation<List<UserDto>>> violations = validator.validate(users);
        users.add(new UserDto());

        assertTrue(violations.isEmpty());
    }
}
