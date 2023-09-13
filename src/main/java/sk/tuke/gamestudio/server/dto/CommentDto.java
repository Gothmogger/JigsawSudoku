package sk.tuke.gamestudio.server.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.User;

import java.time.Instant;

public class CommentDto {
    private String game;

    private String player;

    @NotBlank
    @Size(max = 250, message = "The comment is too long.")
    private String text;

    private Instant createdOn;

    public CommentDto() {

    }

    public CommentDto(String game, String player, String text, Instant createdOn) {
        this(game, player, text);
        this.createdOn = createdOn;
    }

    public CommentDto(String game, String player, String text) {
        this.game = game;
        this.player = player;
        this.text = text;
    }

    public Comment toComment() {
        Comment comment = new Comment();
        comment.setText(text);
        return comment;
    }

    public static CommentDto fromComment(Comment comment) {
        return new CommentDto(comment.getGame(), comment.getPlayer().getUserName(), comment.getText(), comment.getCreatedOn());
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
                ", text=" + text +
                ", commentedOn=" + createdOn +
                '}';
    }
}
