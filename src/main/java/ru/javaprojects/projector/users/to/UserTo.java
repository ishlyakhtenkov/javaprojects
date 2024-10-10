package ru.javaprojects.projector.users.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasIdAndEmail;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.validation.NoHtml;
import ru.javaprojects.projector.users.model.Role;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserTo extends BaseTo implements HasIdAndEmail {
    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String email;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String name;

    @NotEmpty
    private Set<Role> roles;

    public UserTo(Long id, String email, String name, Set<Role> roles) {
        super(id);
        this.email = email;
        this.name = name;
        this.roles = roles;
    }

    @Override
    public String toString() {
        return String.format("UserTo[id=%d, email=%s]", id, email);
    }
}
