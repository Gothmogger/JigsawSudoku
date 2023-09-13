package sk.tuke.gamestudio.server.webservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.dto.ScoreDto;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.List;
import java.util.stream.Collectors;

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
public class ScoreServiceRestTest {
    @Autowired
    private ToJsonUtil toJsonUtil;

    //@MockBean
    @Autowired
    private ScoreService service;
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
    public void shouldReturnTopGameScores() throws Exception {
        User user = new User("UserName", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);
        User user2 = new User("UserNameDif", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user2);
        List<Score> scores = List.of(new Score("JigsawSudoku", user, 100),
                new Score("JigsawSudoku", user2, 50));
        scores.forEach(score -> service.addScore(score));
        //when(service.getTopScores("JigsawSudoku")).thenReturn(scores);

        mvc.perform(get("/api/score/JigsawSudoku"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonUtil.toJson(scores.stream().map(ScoreDto::fromScore).collect(Collectors.toList()))));

        //verify(service, times(1)).getTopScores(any());
    }
/*
    @Test
    @WithMockCustomUser
    public void shouldSucceedStoringScore() throws Exception {
        ScoreDto scoreDto = new ScoreDto();
        scoreDto.setPoints(1000);
        doNothing().when(service).addScore(any(Score.class));

        mvc.perform(post("/api/score").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MVCConfig.toJson(scoreDto)))
                .andExpect(status().isOk());

        verify(service, times(1)).addScore(any(Score.class));
        ArgumentCaptor<Score> argument = ArgumentCaptor.forClass(Score.class);
        verify(service).addScore(argument.capture());
        assertEquals("UserName", argument.getValue().getPlayer().getUserName());
    }

    @Test
    @WithMockUser
    public void shouldFailAddingAInvalidScore() throws Exception {
        ScoreDto scoreDto = new ScoreDto();
        scoreDto.setPoints(-50);

        mvc.perform(post("/api/score").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MVCConfig.toJson(scoreDto)))
                .andExpect(status().isBadRequest());

        verify(service, times(0)).addScore(any(Score.class));
    }

    @Test
    public void shouldFailAddingAScoreWhenNotAuthenticated() throws Exception {
        ScoreDto scoreDto = new ScoreDto();
        scoreDto.setPoints(0);

        mvc.perform(post("/api/score").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MVCConfig.toJson(scoreDto)))
                .andExpect(status().isUnauthorized());

        verify(service, times(0)).addScore(any(Score.class));
    }*/
}
