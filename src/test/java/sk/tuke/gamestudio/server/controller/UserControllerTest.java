package sk.tuke.gamestudio.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.MVCConfig;
import sk.tuke.gamestudio.ToJsonUtil;
import sk.tuke.gamestudio.server.dto.UserDto;

import java.util.HashMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(classes = {MVCConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public class UserControllerTest {
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ToJsonUtil toJsonUtil;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .apply(sharedHttpSession())
                .build();
    }

    @Test
    public void shouldFailWhenNoCSRF() throws Exception {
        mvc.perform(post("/register").with(csrf().useInvalidToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldBeAbleToRegisterAndLogin() throws Exception {
        // MockMVC limitation is that the result does not contain cookies, they are not even set. So, don't test CSRF
        // token in header, since it is only set if JSESSIONID cookie is set as well.
        UserDto userDto = new UserDto("TestUser", "testPassword");
        mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(toJsonUtil.toJson(userDto)))
                .andExpect(status().isOk());
                        //.andExpect(header().exists("X-CSRF-TOKEN"));

        mvc
                .perform(logout().logoutUrl("/api/logout"));

        mvc.perform(formLogin("/api/login").user("userName", "TestUser").password("testPassword"))
                .andExpect(status().isOk());
                //.andExpect(header().exists("X-CSRF-TOKEN"));
    }

    @Test
    public void shouldFailToRegisterWhenInvalidData() throws Exception {
        mvc.perform(post("/api/register").with(csrf()).param("userName", "ab").param("password", "&#48")
                        .param("matchingPassword", "testPassword").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailToRegisterWhenNameInUse() throws Exception {
        UserDto userDto = new UserDto("TestUser", "testPassword");
        mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(toJsonUtil.toJson(userDto)))
                .andExpect(status().isOk());

        mvc
                .perform(logout());

        mvc.perform(post("/api/register").with(csrf()).param("userName", "TestUser").param("password", "testPassword")
                        .param("matchingPassword", "testPassword").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailToLoginWhenIncorrectPassword() throws Exception {
        UserDto userDto = new UserDto("user", "pass");
        mvc.perform(post("/api/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(toJsonUtil.toJson(userDto)))
                .andExpect(status().isOk());

        mvc
                .perform(logout());

        mvc.perform(formLogin("/api/login").user("userName", "user").password("passInc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void shouldFailAccessLoginWhenLoggedIn() throws Exception {
        mvc.perform(formLogin("/api/login").user("user").password("passInc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void shouldFailAccessRegisterWhenLoggedIn() throws Exception {
        mvc.perform(post("/api/register").with(csrf()).param("userName", "user").param("password", "pass")
                        .param("matchingPassword", "pass"))
                .andExpect(status().isBadRequest());
    }
}
