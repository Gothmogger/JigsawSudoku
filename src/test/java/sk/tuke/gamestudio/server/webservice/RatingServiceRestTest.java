package sk.tuke.gamestudio.server.webservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.MVCConfig;
import sk.tuke.gamestudio.ToJsonUtil;
import sk.tuke.gamestudio.WithMockCustomUser;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.dto.RatingDto;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.RatingService;

import java.util.List;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test"})
@SpringBootTest(classes = {MVCConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public class RatingServiceRestTest {
    @Autowired
    private ToJsonUtil toJsonUtil;

    @Autowired
    private RatingService service;
    @Autowired
    private UserDetailsService userService;

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .apply(sharedHttpSession())
                .build();
    }

    @Test
    public void shouldReturnAverageGameRating() throws Exception {
        //when(service.getAverageRating(any())).thenReturn(3);

        mvc.perform(get("/api/rating/JigsawSudoku"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonUtil.toJson(3)));

        //verify(service, times(1)).getAverageRating(any());
    }

    @Test
    public void shouldReturnGameRatingOfPlayer() throws Exception {
        //when(service.getRating(any(), any())).thenReturn(3);
        User user = new User("UserName", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);
        Rating rating = new Rating("JigsawSudoku", user, 4);
        service.setRating(rating);

        mvc.perform(get("/api/rating/JigsawSudoku/UserName"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonUtil.toJson(4)));

        //verify(service, times(1)).getRating(any(), any());
    }

    @Test
    @WithMockCustomUser(name = "Samuel")
    public void shouldSucceedSettingARating() throws Exception {
        ((MyUserDetailsService)userService).registerNewUserAccount(
                ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser());
        RatingDto ratingDto = new RatingDto();
        ratingDto.setStars(3);
        //doNothing().when(service).setRating(any(Rating.class));

        mvc.perform(post("/api/rating").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(ratingDto)))
                .andExpect(status().isOk());

        int ratingPoints = service.getRating("JigsawSudoku", "Samuel");
        assertEquals(ratingPoints, 3);
        /*
        verify(service, times(1)).setRating(any(Rating.class));
        ArgumentCaptor<Rating> argument = ArgumentCaptor.forClass(Rating.class);
        verify(service).setRating(argument.capture());
        assertEquals("Samuel", argument.getValue().getPlayer().getUserName());*/
    }

    @Test
    @WithMockUser
    public void shouldFailAddingAInvalidRating() throws Exception {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setStars(-4);
        //doNothing().when(service).setRating(any(Rating.class));

        mvc.perform(post("/api/rating").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(ratingDto)))
                .andExpect(status().isBadRequest());

        assertEquals(service.getRating("JigsawSudoku", "UserName"), -1);
        //verify(service, times(0)).setRating(any(Rating.class));
    }

    @Test
    public void shouldFailAddingRatingWhenNotAuthenticated() throws Exception {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setStars(5);
        //doNothing().when(service).setRating(any(Rating.class));

        mvc.perform(post("/api/rating").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(ratingDto)))
                .andExpect(status().isUnauthorized());

        assertEquals(service.getAverageRating("JigsawSudoku"), 3);
        //verify(service, times(0)).setRating(any(Rating.class));
    }
}
