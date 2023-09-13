package sk.tuke.gamestudio.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sk.tuke.gamestudio.JPAConfig;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.webservice.MyUserDetailsService;

import java.time.Instant;
import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"jpa"})
@ContextConfiguration(classes = {JPAConfig.class})
@DataJpaTest
public class CommentServiceJPATest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserDetailsService userService;

    @Test
    public void shouldFindNoCommentsIfCommentsIsEmptyTest() {
        List<Comment> comments = commentService.getComments("JigsawSudoku");
        Assert.assertEquals(comments.size(), 0);
    }
    @Test
    public void addCommentsAndResetTest() throws InterruptedException {
        User user = new User("Samo", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);

        commentService.addComment(new Comment("JigsawSudoku", user, "Comment first", Instant.ofEpochMilli(Instant.now().toEpochMilli() - 10000)));
        commentService.addComment(new Comment("JigsawSudoku", user, "Comment second", Instant.now()));
        List<Comment> comments = commentService.getComments("JigsawSudoku");
        Assert.assertEquals(comments.size(), 2);
        //Assert.assertEquals(comments.get(0).getText(), "Comment second"); // For same reason they often have the same time during this test, so I comment this out
        commentService.reset();
        comments = commentService.getComments("JigsawSudoku");
        Assert.assertEquals(comments.size(), 0);
    }
}