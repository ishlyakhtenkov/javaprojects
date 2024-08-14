package ru.javaprojects.projector.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.users.User;

import java.util.Date;

@Entity
@Table(name = "change_email_tokens", uniqueConstraints = @UniqueConstraint(columnNames = "user_id", name = "change_email_tokens_unique_user_idx"))
@Getter
@Setter
@NoArgsConstructor
public class ChangeEmailToken extends BaseEntity {

    @NotBlank
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "new_email", nullable = false)
    private String newEmail;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public ChangeEmailToken(Long id, String token, Date expiryDate, String newEmail, User user) {
        super(id);
        this.token = token;
        this.expiryDate = expiryDate;
        this.newEmail = newEmail;
        this.user = user;
    }

    @Override
    public String toString() {
        return String.format("ChangeEmailToken[id=%d, expiryDate=%s]", id, expiryDate);
    }
}
