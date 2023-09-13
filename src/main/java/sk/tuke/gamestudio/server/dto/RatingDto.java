package sk.tuke.gamestudio.server.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.User;

import java.time.Instant;

public class RatingDto {
    private String game;

    private String player;

    @Min(0)
    @Max(5)
    private int stars;

    private Instant createdOn;

    public RatingDto() {

    }

    public RatingDto(String game, String player, int stars) {
        this.game = game;
        this.player = player;
        this.stars = stars;
    }

    public RatingDto(String game, String player, int stars, Instant createdOn) {
        this(game, player, stars);
        this.createdOn = createdOn;
    }

    public Rating toRating() {
        Rating rating = new Rating();
        rating.setStars(stars);
        return rating;
    }

    public static RatingDto fromRating(Rating rating) {
        return new RatingDto(rating.getGame(), rating.getPlayer().getUserName(), rating.getStars(), rating.getCreatedOn());
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
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
                ", player='" + player + '\'' +
                ", stars=" + stars +
                '}';
    }
}