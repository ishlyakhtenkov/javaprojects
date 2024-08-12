package ru.javaprojects.projector.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.users.User;

import java.util.Date;

@Entity
@Table(name = "password_reset_tokens", uniqueConstraints = {@UniqueConstraint(columnNames = "user_id", name = "password_reset_tokens_unique_user_idx")})
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken extends BaseEntity {

    @NotBlank
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public PasswordResetToken(Long id, String token, Date expiryDate, User user) {
        super(id);
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }

    @Override
    public String toString() {
        return String.format("PasswordResetToken[id=%d, expiryDate=%s]", id, expiryDate);
    }
}
