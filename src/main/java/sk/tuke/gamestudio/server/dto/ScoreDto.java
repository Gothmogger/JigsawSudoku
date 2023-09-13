package sk.tuke.gamestudio.server.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.User;

import java.time.Instant;

public class ScoreDto {
    private String game;

    private String player;

    @Min(0)
    private int points;

    private Instant createdOn;

    public ScoreDto() {

    }

    public ScoreDto(String game, String player, int points, Instant createdOn) {
        this(game, player, points);
        this.createdOn = createdOn;
    }

    public ScoreDto(String game, String player, int points) {
        this.game = game;
        this.player = player;
        this.points = points;
    }

    public Score toScore() {
        Score score = new Score();
        score.setPoints(points);
        return score;
    }

    public static ScoreDto fromScore(Score score) {
        return new ScoreDto(score.getGame(), score.getPlayer().getUserName(), score.getPoints(), score.getCreatedOn());
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
                ", player='" + player + '\'' +
                ", points=" + points +
                ", playedOn=" + createdOn +
                '}';
    }
}