package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;

@Entity
public class Score {
    @Id
    @GeneratedValue
    private int ident;

    @Column(length = 20)
    private String game;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "ident")
    private User player;

    private int points;

    @CreationTimestamp
    private Instant createdOn;

    public Score() {

    }

    public Score(String game, User player, int points, Instant createdOn) {
        this(game, player, points);
        this.createdOn = createdOn;
    }

    public Score(String game, User player, int points) {
        this.game = game;
        this.player = player;
        this.points = points;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
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
                ", points=" + points +
                ", playedOn=" + createdOn +
                '}';
    }

}
