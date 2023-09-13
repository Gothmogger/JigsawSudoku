package sk.tuke.gamestudio.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;

/*
No need to set nullable = false in column annotation, since hibernate applies the jakarta not null and creates the table
with not null already. This can be set off. Also, it will be validated on server before sending to database.
Cascading:
Ked mas Courses a BookCourses. BookCourses ma foreign key na course. On delete cascade - ked vymazes course, vsetky BookCourses
kere na neho maju foreign key sa vymazu. Ked vymazes BookCourses, course sa nevymaze. Da sa mat @OnDelete(action = OnDeleteAction.CASCADE),
coz nastavi delete cascade na table.
Hibernate ale ma svoje:
DELETE on the Room list on House means that if you delete the House then delete all it's Rooms. - toto by som povedal ze
sa sprava tak isto jak sql cascade.     DELETE je hibernates, Remove je JPAs. they are the same
DELETE_ORPHAN on the Room list on House means if you remove a Room from that collection, delete it entirely. Without it,
the Room would still exist but not be attached to anything (hence "orphan").
V Hibernate, napr v tomto pripade je User owning side a comment referencing(inverse) side, ale neni to tym ze comment referencuje
usera. Owning je ten, co ma JoinColumn v sebe. Dobra practice je vraj mat owning side totu co referencuje.
Keby si chcel mat bidirectional, ze aj comment moze sa dostat k svojemu userovi aj user k svojim commentom ta v tom
kde nemas joincolumn by si dal mappedBy = meno fieldu v owning side, cize v tomto pripade player.

oneToMany is lazily loaded, u can set fetch to change this. it is also always optional(u can set comment without assigning
it a user if there was onetomany on user). optional = true is by default even here.

also, user needs to be persisted before u persist comment which references it. u can add cascade = CascadeType.Persist

iba many-to-many relationship involves middle table referencing both other tables.

Manytomany relationship kus nelogicky podla mna ma nastavenie.
*/

@Entity
public class Comment {
    @Id
    @GeneratedValue
    private int ident;

    @Column(length = 20)
    private String game;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "ident")
    private User player;

    @Column(length = 250)
    private String text;

    @CreationTimestamp
    private Instant createdOn;

    public Comment() {

    }

    public Comment(String game, User player, String text, Instant createdOn) {
        this(game, player, text);
        this.createdOn = createdOn;
    }

    public Comment(String game, User player, String text) {
        this.game = game;
        this.player = player;
        this.text = text;
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
                ", player='" + player.getUserName() + '\'' +
                ", text=" + text +
                ", commentedOn=" + createdOn +
                '}';
    }
}
