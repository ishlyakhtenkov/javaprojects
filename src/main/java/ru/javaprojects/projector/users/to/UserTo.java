package ru.javaprojects.projector.users.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.javaprojects.projector.common.HasIdAndEmail;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.util.validation.NoHtml;
import ru.javaprojects.projector.users.model.Role;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserTo extends BaseTo implements HasIdAndEmail {

    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String email;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
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
