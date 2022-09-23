package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.filmorate.model.validators.LoginConstraint;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractData {
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    private String email;

    @LoginConstraint
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @JsonIgnore
    private List<User> friends = new ArrayList<>();

    public void addFriend(User friend) {
        friends.add(friend);
        friend.friends.add(this);
    }

    public void removeFriend(User friend) {
        friends.remove(friend);
        friend.friends.remove(this);
    }
}