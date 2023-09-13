package sk.tuke.gamestudio.server.dto;

import jakarta.validation.constraints.*;
import sk.tuke.gamestudio.entity.User;

public class UserDto {
    // @NotNull - not null je implicit pri notempty a notblank
    @Pattern(regexp="^[A-Za-z0-9]*$",message = "Username can only contain characters and numbers")
    @Size(min = 3, max = 12, message = "Username must be between 3 and 12 characters long")
    @NotNull
    private String userName;
    @Pattern(regexp="^[A-Za-z0-9]*$",message = "Password can only contain characters and numbers")
    @Size(min = 3, max = 12, message = "Password must be between 3 and 12 characters long")
    @NotNull
    private String password;

    public UserDto() {

    }

    public UserDto(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public User toUser() {
        return new User(userName, password);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}