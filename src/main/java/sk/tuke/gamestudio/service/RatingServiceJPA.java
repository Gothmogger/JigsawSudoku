package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.entity.Rating;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.Instant;

@Transactional
public class RatingServiceJPA implements RatingService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsService userService;

    @Override
    public void setRating(Rating rating) {
        Object oldRating = null;
        try {
            oldRating = entityManager.createQuery("select r from Rating r where r.game = :game and r.player = :player")
                    .setParameter("game", rating.getGame())
                    .setParameter("player", rating.getPlayer())
                    .getSingleResult();
            ((Rating) oldRating).setStars(rating.getStars());
            ((Rating) oldRating).setCreatedOn(Instant.now());
        } catch (NoResultException e) {
            entityManager.persist(rating);
        }
    }

    @Override
    public int getAverageRating(String game) {
        Object averageRating = entityManager.createQuery("select AVG(r.stars) from Rating r where r.game = :game")
                .setParameter("game", game)
                .getSingleResult();
        return averageRating != null ? (int) ((double) averageRating) : 3;
    }

    @Override
    public int getRating(String game, String player) {
        Object rating = null;
        try {
            rating = entityManager.createQuery("select r from Rating r where r.game = :game and r.player = :player")
                    .setParameter("game", game)
                    .setParameter("player", ((MyUserDetails)userService.loadUserByUsername(player)).getUser())
                    .getSingleResult();
            return ((Rating) rating).getStars();
        } catch (NoResultException | UsernameNotFoundException e) {
            return -1;
        }
    }

    @Override
    public void reset() {
        entityManager.createNativeQuery("DELETE FROM rating").executeUpdate();
    }
}
