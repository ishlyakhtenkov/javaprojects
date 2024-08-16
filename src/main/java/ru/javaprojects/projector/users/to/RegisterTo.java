package ru.javaprojects.projector.users.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.javaprojects.projector.common.HasEmailAndPassword;
import ru.javaprojects.projector.common.HasIdAndEmail;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.util.validation.NoHtml;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RegisterTo extends BaseTo implements HasIdAndEmail, HasEmailAndPassword {

    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String email;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String name;

    @NotBlank
    @Size(min = 5, max = 32)
    private String password;

    public RegisterTo(Long id, String email, String name, String password) {
        super(id);
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("RegisterTo[id=%d, email=%s]", id, email);
    }
}
