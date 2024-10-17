const enMessages = new Map();
const ruMessages = new Map();

enMessages.set('user.passwords-not-match', 'passwords do not match');
enMessages.set('user.change-password-for', 'Change password for "{0}"');
enMessages.set('user.password-changed', 'Password has been changed');
enMessages.set('user.password-changed-for', 'Password for "{0}" has been changed');
enMessages.set('user.enable', 'Enable user');
enMessages.set('user.disable', 'Disable user');
enMessages.set('user.enabled', 'User "{0}" has been enabled');
enMessages.set('user.disabled', 'User "{0}" has been disabled');
enMessages.set('user.deleted', 'User "{0}" has been deleted');
enMessages.set('user.failed-to-change-password', 'Failed to change password');
enMessages.set('user.failed-to-change-password-for', 'Failed to change password for "{0}"');
enMessages.set('user.failed-to-reset-password', 'Failed to reset password');
enMessages.set('user.failed-to-enable', 'Failed to enable user "{0}"');
enMessages.set('user.failed-to-disable', 'Failed to disable user "{0}"');
enMessages.set('user.failed-to-delete', 'Failed to delete user "{0}"');
enMessages.set('comment.leave-comment-here', 'Leave a comment here');
enMessages.set('comment.delete', 'Delete comment');
enMessages.set('comment.deleted', 'Comment has been deleted');
enMessages.set('comment.failed-to-add', 'Failed to add comment');
enMessages.set('comment.failed-to-update', 'Failed to update comment');
enMessages.set('comment.failed-to-like', 'Failed to like comment');
enMessages.set('comment.failed-to-dislike', 'Failed to dislike comment');
enMessages.set('comment.failed-to-delete', 'Failed to delete comment');
enMessages.set('button.send', 'Send');
enMessages.set('button.cancel', 'Cancel');
enMessages.set('button.like', 'Like');
enMessages.set('button.reply', 'Reply');
enMessages.set('button.edit', 'Edit');
enMessages.set('button.delete', 'Delete');
enMessages.set('project.enable', 'Enable project');
enMessages.set('project.disable', 'Disable project');
enMessages.set('project.enabled', 'Project "{0}" has been enabled');
enMessages.set('project.disabled', 'Project "{0}" has been disabled');
enMessages.set('project.visible-to-users', 'Visible to users');
enMessages.set('project.not-visible-to-users', 'Not visible to users');
enMessages.set('project.deleted', 'Project "{0}" has been deleted');
enMessages.set('project.failed-to-like', 'Failed to like project');
enMessages.set('project.failed-to-dislike', 'Failed to dislike project');
enMessages.set('project.failed-to-enable', 'Failed to enable project "{0}"');
enMessages.set('project.failed-to-disable', 'Failed to disable project "{0}"');
enMessages.set('project.failed-to-delete', 'Failed to delete project "{0}"');
enMessages.set('label.characters-left', 'characters left');
enMessages.set('label.success', 'Success');
enMessages.set('label.error', 'Error');
enMessages.set('label.logo', 'Logo');
enMessages.set('label.docker-compose-file', 'Docker compose file');
enMessages.set('label.title', 'Title');
enMessages.set('label.paragraph', 'Paragraph');
enMessages.set('label.image', 'Image');
enMessages.set('label.change-image', 'Change image');
enMessages.set('label.choose-image', 'Choose image');
enMessages.set('label.move-up', 'Move up');
enMessages.set('label.move-down', 'Move down');
enMessages.set('label.empty-image-elements', 'You have empty image elements');
enMessages.set('architecture.deleted', 'Architecture "{0}" has been deleted');
enMessages.set('architecture.failed-to-delete', 'Failed to delete architecture "{0}"');
enMessages.set('technology.deleted', 'Technology "{0}" has been deleted');
enMessages.set('technology.failed-to-delete', 'Failed to delete technology "{0}"');



ruMessages.set('user.passwords-not-match', 'пароли не совпадают');
ruMessages.set('user.change-password-for', 'Сменить пароль для "{0}"');
ruMessages.set('user.password-changed', 'Пароль сменен');
ruMessages.set('user.password-changed-for', 'Пароль для "{0}" был изменен');
ruMessages.set('user.enable', 'Активировать пользователя');
ruMessages.set('user.disable', 'Заблокировать пользователя');
ruMessages.set('user.enabled', 'Пользователь "{0}" был активирован');
ruMessages.set('user.disabled', 'Пользователь "{0}" был заблокирован');
ruMessages.set('user.deleted', 'Пользователь "{0}" был удален');
ruMessages.set('user.failed-to-change-password', 'Не удалось сменить пароль');
ruMessages.set('user.failed-to-change-password-for', 'Не удалось изменить пароль для "{0}"');
ruMessages.set('user.failed-to-reset-password', 'Не удалось сбросить пароль');
ruMessages.set('user.failed-to-enable', 'Не удалось активировать пользователя "{0}"');
ruMessages.set('user.failed-to-disable', 'Не удалось заблокировать пользователя "{0}"');
ruMessages.set('user.failed-to-delete', 'Не удалось удалить пользователя "{0}"');
ruMessages.set('comment.leave-comment-here', 'Оставьте комментарий здесь');
ruMessages.set('comment.delete', 'Удалить комментарий');
ruMessages.set('comment.deleted', 'Комментарий удален');
ruMessages.set('comment.failed-to-add', 'Не удалось добавить комментарий');
ruMessages.set('comment.failed-to-update', 'Не удалось обновить комментарий');
ruMessages.set('comment.failed-to-like', 'Не удалось дизлайкнуть комментарий');
ruMessages.set('comment.failed-to-dislike', 'Не удалось лайкнуть комментарий');
ruMessages.set('comment.failed-to-delete', 'Не удалось удалить комментарий');
ruMessages.set('button.send', 'Отправить');
ruMessages.set('button.cancel', 'Отмена');
ruMessages.set('button.like', 'Нравится');
ruMessages.set('button.reply', 'Ответить');
ruMessages.set('button.edit', 'Редактировать');
ruMessages.set('button.delete', 'Удалить');
ruMessages.set('project.enable', 'Активировать проект');
ruMessages.set('project.disable', 'Деактивировать проект');
ruMessages.set('project.enabled', 'Проект "{0}" был активирован');
ruMessages.set('project.disabled', 'Проект "{0}" был деактивирован');
ruMessages.set('project.visible-to-users', 'Виден пользователям');
ruMessages.set('project.not-visible-to-users', 'Не виден пользователям');
ruMessages.set('project.deleted', 'Проект "{0}" был удален');
ruMessages.set('project.failed-to-like', 'Не удалось лайкнуть проект');
ruMessages.set('project.failed-to-dislike', 'Не удалось дизлайкнуть проект');
ruMessages.set('project.failed-to-enable', 'Не удалось активировать проект "{0}"');
ruMessages.set('project.failed-to-disable', 'Не удалось деактивировать проект "{0}"');
ruMessages.set('project.failed-to-delete', 'Не удалось удалить проект "{0}"');
ruMessages.set('label.characters-left', 'символов осталось');
ruMessages.set('label.success', 'Успешно');
ruMessages.set('label.error', 'Ошибка');
ruMessages.set('label.logo', 'Логотип');
ruMessages.set('label.docker-compose-file', 'Docker compose файл');
ruMessages.set('label.title', 'Заголовок');
ruMessages.set('label.paragraph', 'Абзац');
ruMessages.set('label.image', 'Картинка');
ruMessages.set('label.change-image', 'Сменить картинку');
ruMessages.set('label.choose-image', 'Выбрать картинку');
ruMessages.set('label.move-up', 'Сместить вверх');
ruMessages.set('label.move-down', 'Сместить вниз');
ruMessages.set('label.empty-image-elements', 'У вас есть пустые элементы-картинки');
ruMessages.set('architecture.deleted', 'Архитектура "{0}" была удалена');
ruMessages.set('architecture.failed-to-delete', 'Не удалось удалить архитектуру "{0}"');
ruMessages.set('technology.deleted', 'Технология "{0}" была удалена');
ruMessages.set('technology.failed-to-delete', 'Не удалось удалить технологию "{0}"');

function getMessage(messageCode, args) {
    let message = locale === 'ru' ? ruMessages.get(messageCode) : enMessages.get(messageCode);
    if (args != null) {
        for (let i = 0; i < args.length; i++) {
            message = message.replace(`{${i}}`, args[i]);
        }
    }
    return message;
}