package sk.tuke.gamestudio.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sk.tuke.gamestudio.JPAConfig;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.webservice.MyUserDetailsService;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"jpa"})
@ContextConfiguration(classes = {JPAConfig.class})
@DataJpaTest
public class ScoreServiceJPATest {
    @Autowired
    private UserDetailsService userService;

    @Autowired
    private ScoreService scoreService;

    @Test
    public void shouldFindNoScoresIfScoresIsEmptyTest() {
        List<Score> highScores = scoreService.getTopScores("JigsawSudoku");
        Assert.assertEquals(highScores.size(), 0);
    }
    @Test
    public void addScoresAndResetTest() {
        User user = new User("Samo", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);
        scoreService.addScore(new Score("JigsawSudoku", user, 44));
        scoreService.addScore(new Score("JigsawSudoku", user, 49));
        List<Score> highScores = scoreService.getTopScores("JigsawSudoku");
        Assert.assertEquals(highScores.size(), 2);
        Assert.assertEquals(highScores.get(0).getPoints(), 49);
        scoreService.reset();
        highScores = scoreService.getTopScores("JigsawSudoku");
        Assert.assertEquals(highScores.size(), 0);
    }
}
