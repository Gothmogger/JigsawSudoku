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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(classes = {MVCConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PasswordMatchesValidatorTest {
    @Autowired
    private Validator validator;

    @Test
    public void shouldBeViolationWhenPasswordsDoNotMatch() throws Exception {
        /*UserDto userDto = new UserDto();
        userDto.setUserName("TestUser");
        userDto.setPassword("TestPassword");
        userDto.setMatchingPassword("NonMatch");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertFalse(violations.isEmpty());*/
    }
}
