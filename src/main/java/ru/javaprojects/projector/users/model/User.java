package ru.javaprojects.projector.users.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import ru.javaprojects.projector.common.HasEmailAndPassword;
import ru.javaprojects.projector.common.HasIdAndEmail;
import ru.javaprojects.projector.common.model.BaseEntity;
import ru.javaprojects.projector.common.model.File;
import ru.javaprojects.projector.common.util.validation.NoHtml;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "users_unique_email_idx")})
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity implements HasIdAndEmail, HasEmailAndPassword {

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

    @JsonIgnore
    @Size(min = 5, max = 128)
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
    private boolean enabled = true;

    @NotNull
    @Column(name = "registered", nullable = false, columnDefinition = "timestamp default now()")
    private Date registered = new Date();

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role"}, name = "user_roles_unique_idx")})
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 200)
    @NotEmpty
    private Set<Role> roles;

    @Nullable
    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "fileName", column = @Column(name = "avatar_file_name")),
            @AttributeOverride(name = "fileLink", column = @Column(name = "avatar_file_link"))
    })
    private File avatar;

    public User(Long id, String email, String name, String password, boolean enabled, Set<Role> roles) {
        super(id);
        this.email = email;
        this.name = name;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles;
    }

    public User(Long id, String email, String name, String password, boolean enabled, Set<Role> roles, File avatar) {
        this(id, email, name, password, enabled, roles);
        this.avatar = avatar;
    }

    public User(User user) {
        this(user.id, user.email, user.name, user.password, user.enabled, user.roles, user.avatar);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, email=%s]", id, email);
    }
}
