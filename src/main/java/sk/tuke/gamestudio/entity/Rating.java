package sk.tuke.gamestudio.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;

@Entity
public class Rating {
    @Id
    @GeneratedValue
    private int ident;

    @Column(length = 20)
    private String game;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "ident")
    private User player;

    private int stars;

    @CreationTimestamp
    private Instant createdOn;

    public Rating() {

    }

    public Rating(String game, User player, int stars) {
        this.game = game;
        this.player = player;
        this.stars = stars;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "Score{" +
                "game='" + game + '\'' +
                ", player='" + player.getUserName() + '\'' +
                ", stars=" + stars +
                ", ratedOn=" + createdOn +
                '}';
    }
}
