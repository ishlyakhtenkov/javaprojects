package ru.javaprojects.projector.users.to;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.projector.common.HasIdAndEmail;
import ru.javaprojects.projector.common.to.BaseTo;
import ru.javaprojects.projector.common.to.FileTo;
import ru.javaprojects.projector.common.validation.ImageFile;
import ru.javaprojects.projector.common.validation.NoHtml;

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
    @Valid
    @ImageFile
    private FileTo avatar;

    public ProfileTo(Long id, String email, String name, String avatarFileName, String avatarFileLink) {
        super(id);
        this.email = email;
        this.name = name;
        this.avatar = new FileTo(avatarFileName, avatarFileLink, null, null);
    }

    @Override
    public String toString() {
        return String.format("ProfileTo[id=%d, email=%s]", id, email);
    }
}
