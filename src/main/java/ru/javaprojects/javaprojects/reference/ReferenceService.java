package ru.javaprojects.javaprojects.reference;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.javaprojects.common.error.IllegalRequestDataException;
import ru.javaprojects.javaprojects.common.util.AppUtil;
import ru.javaprojects.javaprojects.common.util.FileUtil;

import static ru.javaprojects.javaprojects.common.util.FileUtil.normalizePath;

public abstract class ReferenceService<T extends Reference, S extends ReferenceTo> {
    protected final ReferenceRepository<T> repository;
    protected final String filesPath;
    private final String referenceClassName;

    public ReferenceService(ReferenceRepository<T> repository, String filesPath, String referenceClassName) {
        this.repository = repository;
        this.filesPath = filesPath;
        this.referenceClassName = referenceClassName;
    }

    public T get(long id) {
        return repository.getExisted(id);
    }

    @Transactional
    public T create(S referenceTo) {
        Assert.notNull(referenceTo, referenceTo.getClass().getSimpleName() + " must not be null");
        if (referenceTo.getLogo() == null || referenceTo.getLogo().isEmpty()) {
            throw new IllegalRequestDataException(referenceClassName + " logo file is not present",
                    referenceClassName.toLowerCase() + ".logo-not-present", null);
        }
        T reference = repository.saveAndFlush(createNewFromTo(referenceTo));
        FileUtil.upload(referenceTo.getLogo(),
                filesPath + FileUtil.normalizePath(reference.getName() + "/"),
                FileUtil.normalizePath(referenceTo.getLogo().getRealFileName()));
        return reference;
    }

    protected abstract T createNewFromTo(S referenceTo);

    @Transactional
    public T update(S referenceTo) {
        Assert.notNull(referenceTo, "referenceTo must not be null");
        T reference = get(referenceTo.getId());
        String oldName = reference.getName();
        String oldLogoFileLink = reference.getLogo().getFileLink();
        repository.saveAndFlush(updateFromTo(reference, referenceTo));
        if (referenceTo.getLogo() != null && !referenceTo.getLogo().isEmpty()) {
            FileUtil.upload(referenceTo.getLogo(), filesPath + FileUtil.normalizePath(reference.getName() + "/"),
                    FileUtil.normalizePath(referenceTo.getLogo().getRealFileName()));
            if (!oldLogoFileLink.equalsIgnoreCase(reference.getLogo().getFileLink())) {
                FileUtil.deleteFile(oldLogoFileLink);
            }
        } else if (!reference.getName().equalsIgnoreCase(oldName)) {
            FileUtil.moveFile(oldLogoFileLink, filesPath + FileUtil.normalizePath(reference.getName()));
        }
        return reference;
    }

    private T updateFromTo(T reference, S referenceTo) {
        String referenceOldName = reference.getName();
        reference.setName(referenceTo.getName());
        updateSpecificFields(reference, referenceTo);
        if (referenceTo.getLogo() != null && !referenceTo.getLogo().isEmpty()) {
            reference.setLogo(AppUtil.createFile(referenceTo::getLogo, filesPath, referenceTo.getName()));
        } else if (!reference.getName().equalsIgnoreCase(referenceOldName)) {
            reference.getLogo().setFileLink(filesPath + normalizePath(reference.getName() + "/" +
                    reference.getLogo().getFileName()));
        }
        return reference;
    }

    protected abstract void updateSpecificFields(T reference, S referenceTo);

    @Transactional
    public void delete(long id) {
        T reference = get(id);
        repository.delete(reference);
        repository.flush();
        FileUtil.deleteFile(reference.getLogo().getFileLink());
    }
}
