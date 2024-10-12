package ru.javaprojects.projector.users.model.token;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.model.BaseEntity;

import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Token extends BaseEntity {
    @NotBlank
    @Column(name = "token", nullable = false)
    protected String token;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    protected Date expiryDate;

    public Token(Long id, String token, Date expiryDate) {
        super(id);
        this.token = token;
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, expiryDate=%s]", getClass().getSimpleName(),id, expiryDate);
    }
}
