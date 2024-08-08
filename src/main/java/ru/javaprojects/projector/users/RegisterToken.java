package ru.javaprojects.projector.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.util.validation.NoHtml;

import java.util.Date;

@Entity
@Table(name = "register_tokens", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "register_tokens_unique_email_idx")})
@Getter
@Setter
@NoArgsConstructor
public class RegisterToken extends BaseEntity {

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
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(min = 5, max = 128)
    @Column(name = "password", nullable = false)
    private String password;

    public RegisterToken(Long id, String token, Date expiryDate, String email, String name, String password) {
        super(id);
        this.token = token;
        this.expiryDate = expiryDate;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("RegisterToken[id=%d, expiryDate=%s]", id, expiryDate);
    }
}
