package sk.tuke.gamestudio.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sk.tuke.gamestudio.JPAConfig;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.server.webservice.MyUserDetailsService;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"jpa"})
@ContextConfiguration(classes = {JPAConfig.class})
@DataJpaTest
public class RatingServiceJPATest {
    @Autowired
    private RatingService ratingService;
    @Autowired
    private UserDetailsService userService;

    @Test
    public void shouldFindNoRatingsIfNoRatingsArePresentTest() {
        User user = new User("Samo", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);

        Assert.assertEquals(ratingService.getAverageRating("JigsawSudoku"), 3);
        Assert.assertEquals(ratingService.getRating("JigsawSudoku", user.getUserName()), -1);
    }
    @Test
    public void addRatingsAndResetTest() {
        User user = new User("Samo", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user);
        User user2 = new User("Jaro", "pass");
        ((MyUserDetailsService)userService).registerNewUserAccount(user2);

        ratingService.setRating(new Rating("JigsawSudoku", user, 5));
        ratingService.setRating(new Rating("JigsawSudoku", user, 0));
        Assert.assertEquals(ratingService.getAverageRating("JigsawSudoku"), 0);
        ratingService.setRating(new Rating("JigsawSudoku", user2, 5));
        Assert.assertEquals(ratingService.getAverageRating("JigsawSudoku"), 2);

        ratingService.setRating(new Rating("Reversi", user2, 5));
        Assert.assertEquals(ratingService.getAverageRating("Reversi"), 5);
        ratingService.reset();
        Assert.assertEquals(ratingService.getAverageRating("JigsawSudoku"), 3);
        Assert.assertEquals(ratingService.getAverageRating("Reversi"), 3);
    }
}