package ru.javaprojects.projector.common.util;

import ru.javaprojects.projector.common.HasIdAndParentId;

import java.util.List;

public interface TreeNode<T extends HasIdAndParentId, R extends TreeNode<T, R>> {
    List<R> subNodes();
}
