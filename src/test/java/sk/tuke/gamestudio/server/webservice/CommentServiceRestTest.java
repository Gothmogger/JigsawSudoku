package sk.tuke.gamestudio.server.webservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
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
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.SecurityConfiguration;
import sk.tuke.gamestudio.server.dto.CommentDto;
import sk.tuke.gamestudio.server.dto.TileDto;
import sk.tuke.gamestudio.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
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
public class CommentServiceRestTest {
    @Autowired
    private ToJsonUtil toJsonUtil;

    @Autowired
    private CommentService service;
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
    public void shouldReturnGameComments() throws Exception {
        User user = new User("UserName", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);
        User user2 = new User("UserNameDif", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user2);
        List<Comment> comments = List.of(new Comment("JigsawSudoku", user, "Comment1 Text"),
                new Comment("JigsawSudoku", user2, "Comment2 Text"));
        //when(service.getComments("JigsawSudoku")).thenReturn(comments);
        comments.forEach(comment -> service.addComment(comment));

        mvc.perform(get("/api/comment/JigsawSudoku"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJsonUtil.toJson(comments.stream().map(CommentDto::fromComment).collect(Collectors.toList()))));

        //verify(service, times(1)).getComments(any());
    }

    @Test
    @WithMockCustomUser
    public void shouldSucceedAddingAComment() throws Exception {
        ((MyUserDetailsService)userService).registerNewUserAccount(
                ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser());
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Comment1 Text");
        //doNothing().when(service).addComment(any(Comment.class));

        mvc.perform(post("/api/comment").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(commentDto)))
                .andExpect(status().isOk());

        List<Comment> comments = service.getComments("JigsawSudoku");
        assertEquals(comments.size(), 1);
        assertEquals(comments.get(0).getPlayer().getUserName(), "UserName");
        assertEquals(comments.get(0).getText(), "Comment1 Text");
/*
        verify(service, times(1)).addComment(any(Comment.class));
        ArgumentCaptor<Comment> argument = ArgumentCaptor.forClass(Comment.class);
        verify(service).addComment(argument.capture());
        assertEquals("UserName", argument.getValue().getPlayer().getUserName());*/
    }

    @Test
    @WithMockUser
    public void shouldFailAddingAInvalidComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("");

        mvc.perform(post("/api/comment").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(commentDto)))
                .andExpect(status().isBadRequest());

        List<Comment> comments = service.getComments("JigsawSudoku");
        assertEquals(comments.size(), 0);

        //verify(service, times(0)).addComment(any(Comment.class));
    }

    @Test
    public void shouldFailAddingACommentWhenNotAuthenticated() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Comment1");

        mvc.perform(post("/api/comment").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonUtil.toJson(commentDto)))
                .andExpect(status().isUnauthorized());

        List<Comment> comments = service.getComments("JigsawSudoku");
        assertEquals(comments.size(), 0);
        //verify(service, times(0)).addComment(any(Comment.class));
    }
}
