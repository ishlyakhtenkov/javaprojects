package ru.javaprojects.javaprojects.users.to;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.javaprojects.common.HasIdAndEmail;
import ru.javaprojects.javaprojects.common.to.BaseTo;
import ru.javaprojects.javaprojects.common.to.FileTo;
import ru.javaprojects.javaprojects.common.validation.ImageFile;
import ru.javaprojects.javaprojects.common.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class ProfileTo extends BaseTo implements HasIdAndEmail {
    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String email;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String name;

    @Nullable
    @NoHtml
    @Size(max = 4096)
    private String information;

    @Nullable
    @Valid
    @ImageFile
    private FileTo avatar;

    public ProfileTo(Long id, String email, String name, String information, FileTo avatar) {
        super(id);
        this.email = email;
        this.name = name;
        this.information = information;
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return String.format("ProfileTo[id=%d, email=%s]", id, email);
    }
}
